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

    private static final Map<String, String> DEFAULT_BILL_NAME_MAP = new HashMap<>();
    private static final String[] DEFAULT_BILL_CODES = new String[] {
            "LSCK", "LSTH", "CGDD", "CGRK", "CGTH", "XSDD", "XSCK", "XSTH",
            "QTRK", "QTCK", "DBCK", "ZZD", "CXD", "SYF", "QGD", "SR", "ZC", "SK", "FK", "ZZ"
    };

    static {
        DEFAULT_BILL_NAME_MAP.put("LSCK", "零售出库");
        DEFAULT_BILL_NAME_MAP.put("LSTH", "零售退货");
        DEFAULT_BILL_NAME_MAP.put("QGD", "请购单");
        DEFAULT_BILL_NAME_MAP.put("CGDD", "采购订单");
        DEFAULT_BILL_NAME_MAP.put("CGRK", "采购入库");
        DEFAULT_BILL_NAME_MAP.put("CGTH", "采购退货");
        DEFAULT_BILL_NAME_MAP.put("XSDD", "销售订单");
        DEFAULT_BILL_NAME_MAP.put("XSCK", "销售出库");
        DEFAULT_BILL_NAME_MAP.put("XSTH", "销售退货");
        DEFAULT_BILL_NAME_MAP.put("QTRK", "其它入库单");
        DEFAULT_BILL_NAME_MAP.put("QTCK", "其它出库单");
        DEFAULT_BILL_NAME_MAP.put("DBCK", "调拨出库");
        DEFAULT_BILL_NAME_MAP.put("ZZD", "组装单");
        DEFAULT_BILL_NAME_MAP.put("CXD", "拆卸单");
        DEFAULT_BILL_NAME_MAP.put("SR", "收入单");
        DEFAULT_BILL_NAME_MAP.put("ZC", "支出单");
        DEFAULT_BILL_NAME_MAP.put("SK", "收款单");
        DEFAULT_BILL_NAME_MAP.put("FK", "付款单");
        DEFAULT_BILL_NAME_MAP.put("ZZ", "转账单");
        DEFAULT_BILL_NAME_MAP.put("SYF", "收预付款单");
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
        Long tenantId = getTenantId();
        List<ApprovalConfig> configs = approvalMapperEx.selectConfigs(tenantId);
        JSONArray data = new JSONArray();
        for (ApprovalConfig config : configs) {
            JSONObject row = JSONObject.parseObject(JSONObject.toJSONString(config));
            row.put("steps", approvalMapperEx.selectConfigSteps(config.getModuleKey(), tenantId));
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
            throw new RuntimeException("适用单据不能为空");
        }
        if (StringUtil.isEmpty(config.getModuleName())) {
            config.setModuleName(DEFAULT_BILL_NAME_MAP.get(config.getModuleKey()));
        }
        if (StringUtil.isEmpty(config.getBillSubType())) {
            config.setBillSubType(config.getModuleName());
        }
        config.setTenantId(getTenantId());
        config.setEnabled(config.getEnabled() == null || config.getEnabled());
        ApprovalConfig oldConfig = null;
        if (config.getId() == null) {
            approvalMapperEx.insertConfig(config);
        } else {
            oldConfig = getExistingConfig(config.getId());
            approvalMapperEx.updateConfig(config);
        }
        if (oldConfig != null && !config.getModuleKey().equals(oldConfig.getModuleKey())) {
            approvalMapperEx.deleteConfigSteps(oldConfig.getModuleKey(), getTenantId());
        }
        saveConfigSteps(config, jsonObject.getJSONArray("steps"));
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = config;
        return res;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BaseResponseInfo deleteConfig(JSONObject jsonObject) throws Exception {
        Long id = jsonObject.getLong("id");
        if (id == null) {
            throw new RuntimeException("审批配置不能为空");
        }
        ApprovalConfig config = getExistingConfig(id);
        if (config == null) {
            throw new RuntimeException("审批配置不存在");
        }
        approvalMapperEx.deleteConfig(id, getTenantId());
        approvalMapperEx.deleteConfigSteps(config.getModuleKey(), getTenantId());
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        return res;
    }

    private ApprovalConfig getExistingConfig(Long id) throws Exception {
        List<ApprovalConfig> configs = approvalMapperEx.selectConfigs(getTenantId());
        for (ApprovalConfig config : configs) {
            if (id.equals(config.getId())) {
                return config;
            }
        }
        return null;
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
            String moduleKey = parseBillCode(bill.getNumber(), parseDepotModuleKey(bill));
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
            String moduleKey = parseBillCode(bill.getBillNo(), parseAccountModuleKey(bill));
            fillTaskBase(task, moduleKey, bill.getType(), "", bill.getBillNo(),
                    bill.getTotalPrice() == null ? bill.getChangeAmount() : bill.getTotalPrice());
            return task;
        }

        throw new RuntimeException("暂不支持该单据类型");
    }

    private void fillTaskBase(ApprovalTask task, String moduleKey, String billType, String billSubType,
                              String billNo, BigDecimal billAmount) throws Exception {
        ApprovalConfig config = approvalMapperEx.selectConfigByModule(getTenantId(), moduleKey);
        if (config == null) {
            throw new RuntimeException("该单据类型未配置审批流程");
        }
        if (config != null && Boolean.FALSE.equals(config.getEnabled())) {
            throw new RuntimeException("该单据类型未启用审批");
        }
        List<ApprovalTaskStep> steps = getApprovalSteps(moduleKey, config);
        task.setModuleKey(moduleKey);
        task.setModuleName(config.getModuleName());
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
                ? config.getApproverRoleName() : null);
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
        if ("请购单".equals(subType)) {
            return "QGD";
        }
        if ("采购订单".equals(subType)) {
            return "CGDD";
        }
        if ("采购".equals(subType)) {
            return "CGRK";
        }
        if ("采购退货".equals(subType)) {
            return "CGTH";
        }
        if ("销售订单".equals(subType)) {
            return "XSDD";
        }
        if ("销售".equals(subType)) {
            return "XSCK";
        }
        if ("销售退货".equals(subType)) {
            return "XSTH";
        }
        if ("零售".equals(subType)) {
            return "LSCK";
        }
        if ("零售退货".equals(subType)) {
            return "LSTH";
        }
        if ("调拨".equals(subType)) {
            return "DBCK";
        }
        if ("组装单".equals(subType)) {
            return "ZZD";
        }
        if ("拆卸单".equals(subType)) {
            return "CXD";
        }
        if ("其它".equals(subType) && "入库".equals(bill.getType())) {
            return "QTRK";
        }
        if ("其它".equals(subType) && "出库".equals(bill.getType())) {
            return "QTCK";
        }
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

    private String parseAccountModuleKey(AccountHead bill) {
        String type = bill.getType();
        if ("收入".equals(type)) {
            return "SR";
        }
        if ("支出".equals(type)) {
            return "ZC";
        }
        if ("收款".equals(type)) {
            return "SK";
        }
        if ("付款".equals(type)) {
            return "FK";
        }
        if ("转账".equals(type)) {
            return "ZZ";
        }
        if ("收预付款".equals(type)) {
            return "SYF";
        }
        return "finance";
    }

    private String parseBillCode(String billNo, String fallback) {
        if (StringUtil.isEmpty(billNo)) {
            return fallback;
        }
        for (String billCode : DEFAULT_BILL_CODES) {
            if (billNo.startsWith(billCode)) {
                return billCode;
            }
        }
        return fallback;
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
        return null;
    }
}
