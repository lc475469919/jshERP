package com.jsh.erp.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.AccountHeadMapper;
import com.jsh.erp.datasource.mappers.ApprovalMapperEx;
import com.jsh.erp.datasource.mappers.DepotHeadMapper;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApprovalService {
    private static final String BILL_TABLE_DEPOT_HEAD = "depot_head";
    private static final String BILL_TABLE_ACCOUNT_HEAD = "account_head";
    private static final String TASK_PENDING = "pending";
    private static final String TASK_APPROVED = "approved";
    private static final String TASK_REJECTED = "rejected";

    private static final Map<String, String> DEFAULT_ROLE_MAP = new HashMap<>();
    private static final Map<String, String> DEFAULT_NAME_MAP = new HashMap<>();

    static {
        DEFAULT_ROLE_MAP.put("purchase", "采购经理");
        DEFAULT_ROLE_MAP.put("sale", "销售经理");
        DEFAULT_ROLE_MAP.put("retail", "销售经理");
        DEFAULT_ROLE_MAP.put("stock", "仓库主管");
        DEFAULT_ROLE_MAP.put("finance", "财务经理");

        DEFAULT_NAME_MAP.put("purchase", "采购模块");
        DEFAULT_NAME_MAP.put("sale", "销售模块");
        DEFAULT_NAME_MAP.put("retail", "零售模块");
        DEFAULT_NAME_MAP.put("stock", "库存模块");
        DEFAULT_NAME_MAP.put("finance", "财务模块");
    }

    @Resource
    private ApprovalMapperEx approvalMapperEx;
    @Resource
    private DepotHeadMapper depotHeadMapper;
    @Resource
    private AccountHeadMapper accountHeadMapper;
    @Resource
    private DepotHeadService depotHeadService;
    @Resource
    private AccountHeadService accountHeadService;
    @Resource
    private UserService userService;

    public BaseResponseInfo listConfigs() throws Exception {
        List<ApprovalConfig> configs = approvalMapperEx.selectConfigs(getTenantId());
        JSONArray data = new JSONArray();
        for (ApprovalConfig config : configs) {
            JSONObject row = JSONObject.parseObject(JSONObject.toJSONString(config));
            row.put("steps", approvalMapperEx.selectConfigSteps(config.getModuleKey(), getTenantId()));
            data.add(row);
        }
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = data;
        return res;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BaseResponseInfo saveConfig(JSONObject jsonObject) throws Exception {
        ApprovalConfig config = JSONObject.parseObject(jsonObject.toJSONString(), ApprovalConfig.class);
        if (StringUtil.isEmpty(config.getModuleKey())) {
            throw new RuntimeException("模块不能为空");
        }
        config.setTenantId(getTenantId());
        config.setEnabled(config.getEnabled() == null || config.getEnabled());
        if (config.getId() == null) {
            approvalMapperEx.insertConfig(config);
        } else {
            approvalMapperEx.updateConfig(config);
        }
        saveConfigSteps(config, jsonObject.getJSONArray("steps"));
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = config;
        return res;
    }

    private void saveConfigSteps(ApprovalConfig config, JSONArray steps) throws Exception {
        approvalMapperEx.deleteConfigSteps(config.getModuleKey(), getTenantId());
        if (steps == null || steps.isEmpty()) {
            if (config.getApproverRoleId() == null && StringUtil.isEmpty(config.getApproverRoleName())) {
                throw new RuntimeException("审批流程至少需要一个审批角色");
            }
            ApprovalTaskStep step = new ApprovalTaskStep();
            step.setModuleKey(config.getModuleKey());
            step.setStepNo(1);
            step.setApproverRoleId(config.getApproverRoleId());
            step.setApproverRoleName(config.getApproverRoleName());
            step.setTenantId(getTenantId());
            approvalMapperEx.insertConfigStep(step);
            return;
        }
        int stepNo = 1;
        for (int i = 0; i < steps.size(); i++) {
            JSONObject item = steps.getJSONObject(i);
            if (item == null) {
                continue;
            }
            Long roleId = item.getLong("approverRoleId");
            String roleName = item.getString("approverRoleName");
            if (roleId == null && StringUtil.isEmpty(roleName)) {
                continue;
            }
            ApprovalTaskStep step = new ApprovalTaskStep();
            step.setModuleKey(config.getModuleKey());
            step.setStepNo(stepNo++);
            step.setApproverRoleId(roleId);
            step.setApproverRoleName(roleName);
            step.setTenantId(getTenantId());
            approvalMapperEx.insertConfigStep(step);
        }
        if (stepNo == 1) {
            throw new RuntimeException("审批流程至少需要一个审批角色");
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BaseResponseInfo submit(JSONObject jsonObject) throws Exception {
        String billTable = jsonObject.getString("billTable");
        Long billId = jsonObject.getLong("billId");
        String remark = jsonObject.getString("remark");
        if (StringUtil.isEmpty(billTable) || billId == null) {
            throw new RuntimeException("单据信息不完整");
        }
        ApprovalTask oldTask = approvalMapperEx.selectPendingTaskByBill(billTable, billId, getTenantId());
        if (oldTask != null) {
            throw new RuntimeException("该单据已在审批中");
        }

        ApprovalTask task = buildTask(billTable, billId, remark);
        approvalMapperEx.insertTask(task);
        createTaskSteps(task);
        setBillStatus(billTable, billId, "9");

        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = task;
        return res;
    }

    public BaseResponseInfo myTasks(String status) throws Exception {
        Role role = getCurrentRole();
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = approvalMapperEx.selectTasks(status, null, role.getId(), role.getName(), getTenantId());
        return res;
    }

    public BaseResponseInfo submittedTasks(String status) throws Exception {
        User user = userService.getCurrentUser();
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = approvalMapperEx.selectTasks(status, user.getId(), null, null, getTenantId());
        return res;
    }

    public BaseResponseInfo countPending() throws Exception {
        Role role = getCurrentRole();
        JSONObject data = new JSONObject();
        data.put("count", approvalMapperEx.countTasks(TASK_PENDING, role.getId(), role.getName(), getTenantId()));
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = data;
        return res;
    }

    public BaseResponseInfo latestTask(String billTable, Long billId) throws Exception {
        if (StringUtil.isEmpty(billTable) || billId == null) {
            throw new RuntimeException("单据信息不完整");
        }
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = approvalMapperEx.selectLatestTaskByBill(billTable, billId, getTenantId());
        return res;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BaseResponseInfo approve(JSONObject jsonObject) throws Exception {
        return finishTask(jsonObject.getLong("id"), TASK_APPROVED, jsonObject.getString("approveRemark"));
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BaseResponseInfo reject(JSONObject jsonObject) throws Exception {
        return finishTask(jsonObject.getLong("id"), TASK_REJECTED, jsonObject.getString("approveRemark"));
    }

    private BaseResponseInfo finishTask(Long id, String status, String approveRemark) throws Exception {
        if (id == null) {
            throw new RuntimeException("审批任务不能为空");
        }
        ApprovalTask task = approvalMapperEx.selectTaskById(id, getTenantId());
        if (task == null || !TASK_PENDING.equals(task.getStatus())) {
            throw new RuntimeException("审批任务不存在或已处理");
        }
        ApprovalTaskStep currentStep = approvalMapperEx.selectTaskStep(task.getId(), task.getCurrentStepNo(), getTenantId());
        if (currentStep == null) {
            currentStep = createFallbackTaskStep(task);
        }
        if (currentStep == null || !TASK_PENDING.equals(currentStep.getStatus())) {
            throw new RuntimeException("当前审批步骤不存在或已处理");
        }
        assertCurrentRoleCanApprove(currentStep);

        User user = userService.getCurrentUser();
        currentStep.setStatus(status);
        currentStep.setApproverId(user.getId());
        currentStep.setApproverName(user.getUsername());
        currentStep.setApproveRemark(approveRemark);
        approvalMapperEx.updateTaskStepStatus(currentStep);

        if (TASK_REJECTED.equals(status)) {
            task.setStatus(TASK_REJECTED);
            task.setApproverId(user.getId());
            task.setApproverName(user.getUsername());
            task.setApproveRemark(approveRemark);
            approvalMapperEx.updateTaskStatus(task);
            setBillStatus(task.getBillTable(), task.getBillId(), "0");
        } else if (task.getCurrentStepNo() != null && task.getTotalStep() != null
                && task.getCurrentStepNo() < task.getTotalStep()) {
            Integer nextStepNo = task.getCurrentStepNo() + 1;
            ApprovalTaskStep nextStep = approvalMapperEx.selectTaskStep(task.getId(), nextStepNo, getTenantId());
            if (nextStep == null) {
                throw new RuntimeException("下一审批步骤不存在");
            }
            approvalMapperEx.activateTaskStep(task.getId(), nextStepNo, getTenantId());
            task.setCurrentStepNo(nextStepNo);
            task.setApproverRoleId(nextStep.getApproverRoleId());
            task.setApproverRoleName(nextStep.getApproverRoleName());
            approvalMapperEx.updateTaskCurrentStep(task);
        } else {
            task.setStatus(TASK_APPROVED);
            task.setApproverId(user.getId());
            task.setApproverName(user.getUsername());
            task.setApproveRemark(approveRemark);
            approvalMapperEx.updateTaskStatus(task);
            setBillStatus(task.getBillTable(), task.getBillId(), "0");
            if (BILL_TABLE_DEPOT_HEAD.equals(task.getBillTable())) {
                depotHeadService.batchSetStatus("1", task.getBillId().toString());
            } else if (BILL_TABLE_ACCOUNT_HEAD.equals(task.getBillTable())) {
                accountHeadService.batchSetStatus("1", task.getBillId().toString());
            }
        }

        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = task;
        return res;
    }

    private ApprovalTask buildTask(String billTable, Long billId, String remark) throws Exception {
        User user = userService.getCurrentUser();
        ApprovalTask task = new ApprovalTask();
        task.setBillTable(billTable);
        task.setBillId(billId);
        task.setSubmitterId(user.getId());
        task.setSubmitterName(user.getUsername());
        task.setStatus(TASK_PENDING);
        task.setRemark(remark);
        task.setTenantId(getTenantId());

        if (BILL_TABLE_DEPOT_HEAD.equals(billTable)) {
            DepotHead bill = depotHeadMapper.selectByPrimaryKey(billId);
            if (bill == null) {
                throw new RuntimeException("业务单据不存在");
            }
            if ("1".equals(bill.getStatus())) {
                throw new RuntimeException("已审核单据不能提交审批");
            }
            String moduleKey = parseDepotModuleKey(bill);
            fillTaskBase(task, moduleKey, bill.getType(), bill.getSubType(), bill.getNumber(),
                    bill.getDiscountLastMoney() == null ? bill.getTotalPrice() : bill.getDiscountLastMoney());
            return task;
        }

        if (BILL_TABLE_ACCOUNT_HEAD.equals(billTable)) {
            AccountHead bill = accountHeadMapper.selectByPrimaryKey(billId);
            if (bill == null) {
                throw new RuntimeException("财务单据不存在");
            }
            if ("1".equals(bill.getStatus())) {
                throw new RuntimeException("已审核单据不能提交审批");
            }
            fillTaskBase(task, "finance", bill.getType(), "", bill.getBillNo(),
                    bill.getTotalPrice() == null ? bill.getChangeAmount() : bill.getTotalPrice());
            return task;
        }

        throw new RuntimeException("暂不支持该单据类型");
    }

    private void fillTaskBase(ApprovalTask task, String moduleKey, String billType, String billSubType,
                              String billNo, BigDecimal billAmount) throws Exception {
        ApprovalConfig config = approvalMapperEx.selectConfigByModule(getTenantId(), moduleKey);
        if (config != null && Boolean.FALSE.equals(config.getEnabled())) {
            throw new RuntimeException("该模块未启用审批");
        }
        List<ApprovalTaskStep> steps = getApprovalSteps(moduleKey, config);
        task.setModuleKey(moduleKey);
        task.setModuleName(config != null ? config.getModuleName() : DEFAULT_NAME_MAP.get(moduleKey));
        task.setBillType(billType);
        task.setBillSubType(billSubType);
        task.setBillNo(billNo);
        task.setBillAmount(billAmount);
        task.setCurrentStepNo(1);
        task.setTotalStep(steps.size());
        task.setApproverRoleId(steps.get(0).getApproverRoleId());
        task.setApproverRoleName(steps.get(0).getApproverRoleName());
    }

    private List<ApprovalTaskStep> getApprovalSteps(String moduleKey, ApprovalConfig config) throws Exception {
        List<ApprovalTaskStep> steps = approvalMapperEx.selectConfigSteps(moduleKey, getTenantId());
        if (steps != null && !steps.isEmpty()) {
            return steps;
        }
        List<ApprovalTaskStep> fallback = new ArrayList<>();
        ApprovalTaskStep step = new ApprovalTaskStep();
        step.setModuleKey(moduleKey);
        step.setStepNo(1);
        step.setApproverRoleId(config == null ? null : config.getApproverRoleId());
        step.setApproverRoleName(config != null && StringUtil.isNotEmpty(config.getApproverRoleName())
                ? config.getApproverRoleName() : DEFAULT_ROLE_MAP.get(moduleKey));
        if (step.getApproverRoleId() == null && StringUtil.isEmpty(step.getApproverRoleName())) {
            throw new RuntimeException("该模块未配置审批角色");
        }
        fallback.add(step);
        return fallback;
    }

    private void createTaskSteps(ApprovalTask task) throws Exception {
        List<ApprovalTaskStep> steps = getApprovalSteps(task.getModuleKey(),
                approvalMapperEx.selectConfigByModule(getTenantId(), task.getModuleKey()));
        for (int i = 0; i < steps.size(); i++) {
            ApprovalTaskStep source = steps.get(i);
            ApprovalTaskStep step = new ApprovalTaskStep();
            step.setTaskId(task.getId());
            step.setModuleKey(task.getModuleKey());
            step.setStepNo(i + 1);
            step.setApproverRoleId(source.getApproverRoleId());
            step.setApproverRoleName(source.getApproverRoleName());
            step.setStatus(i == 0 ? TASK_PENDING : "waiting");
            step.setTenantId(task.getTenantId());
            approvalMapperEx.insertTaskStep(step);
        }
    }

    private ApprovalTaskStep createFallbackTaskStep(ApprovalTask task) throws Exception {
        task.setCurrentStepNo(task.getCurrentStepNo() == null ? 1 : task.getCurrentStepNo());
        task.setTotalStep(task.getTotalStep() == null ? 1 : task.getTotalStep());
        approvalMapperEx.updateTaskCurrentStep(task);
        ApprovalTaskStep step = new ApprovalTaskStep();
        step.setTaskId(task.getId());
        step.setModuleKey(task.getModuleKey());
        step.setStepNo(task.getCurrentStepNo());
        step.setApproverRoleId(task.getApproverRoleId());
        step.setApproverRoleName(task.getApproverRoleName());
        step.setStatus(TASK_PENDING);
        step.setTenantId(task.getTenantId());
        approvalMapperEx.insertTaskStep(step);
        return step;
    }

    private String parseDepotModuleKey(DepotHead bill) {
        String subType = bill.getSubType();
        if (subType != null && subType.contains("采购")) {
            return "purchase";
        }
        if (subType != null && subType.contains("销售")) {
            return "sale";
        }
        if (subType != null && subType.contains("零售")) {
            return "retail";
        }
        return "stock";
    }

    private void setBillStatus(String billTable, Long billId, String status) {
        if (BILL_TABLE_DEPOT_HEAD.equals(billTable)) {
            DepotHead bill = new DepotHead();
            bill.setId(billId);
            bill.setStatus(status);
            depotHeadMapper.updateByPrimaryKeySelective(bill);
        } else if (BILL_TABLE_ACCOUNT_HEAD.equals(billTable)) {
            AccountHead bill = new AccountHead();
            bill.setId(billId);
            bill.setStatus(status);
            accountHeadMapper.updateByPrimaryKeySelective(bill);
        }
    }

    private void assertCurrentRoleCanApprove(ApprovalTaskStep task) throws Exception {
        Role role = getCurrentRole();
        if (task.getApproverRoleId() != null && role.getId() != null && task.getApproverRoleId().equals(role.getId())) {
            return;
        }
        if (StringUtil.isNotEmpty(task.getApproverRoleName()) && task.getApproverRoleName().equals(role.getName())) {
            return;
        }
        throw new RuntimeException("当前角色不是该模块审批人");
    }

    private Role getCurrentRole() throws Exception {
        User user = userService.getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new RuntimeException("当前用户未登录");
        }
        Role role = userService.getRoleTypeByUserId(user.getId());
        if (role == null || (role.getId() == null && StringUtil.isEmpty(role.getName()))) {
            throw new RuntimeException("当前用户未配置审批角色");
        }
        return role;
    }

    private Long getTenantId() throws Exception {
        User user = userService.getCurrentUser();
        return user == null ? null : (user.getTenantId() == null ? user.getId() : user.getTenantId());
    }
}
