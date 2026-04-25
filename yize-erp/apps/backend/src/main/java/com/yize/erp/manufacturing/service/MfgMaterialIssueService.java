package com.yize.erp.manufacturing.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yize.erp.common.PageResult;
import com.yize.erp.manufacturing.domain.MfgMaterialIssue;
import com.yize.erp.manufacturing.domain.MfgMaterialIssueItem;
import com.yize.erp.manufacturing.domain.MfgTask;
import com.yize.erp.manufacturing.dto.MfgMaterialIssueCreateRequest;
import com.yize.erp.manufacturing.dto.MfgMaterialIssueQueryRequest;
import com.yize.erp.manufacturing.dto.MfgMaterialIssueResponse;
import com.yize.erp.manufacturing.mapper.MfgMaterialIssueItemMapper;
import com.yize.erp.manufacturing.mapper.MfgMaterialIssueMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MfgMaterialIssueService extends ServiceImpl<MfgMaterialIssueMapper, MfgMaterialIssue> {
    private final MfgMaterialIssueItemMapper itemMapper;
    private final MfgTaskService taskService;

    public MfgMaterialIssueService(MfgMaterialIssueItemMapper itemMapper, MfgTaskService taskService) {
        this.itemMapper = itemMapper;
        this.taskService = taskService;
    }

    public PageResult<MfgMaterialIssue> pageIssues(MfgMaterialIssueQueryRequest query) {
        QueryWrapper<MfgMaterialIssue> wrapper = new QueryWrapper<MfgMaterialIssue>()
            .eq(query.taskId() != null, "task_id", query.taskId())
            .eq(StringUtils.hasText(query.status()), "status", query.status())
            .like(StringUtils.hasText(query.keyword()), "issue_no", query.keyword())
            .orderByDesc("issue_time")
            .orderByDesc("id");
        Page<MfgMaterialIssue> result = page(new Page<>(query.safePageNo(), query.safePageSize()), wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public MfgMaterialIssueResponse detail(Long id) {
        MfgMaterialIssue issue = requiredIssue(id);
        return new MfgMaterialIssueResponse(issue, items(id));
    }

    @Transactional
    public MfgMaterialIssueResponse createIssue(MfgMaterialIssueCreateRequest request) {
        MfgTask task = taskService.requiredTask(request.taskId());
        MfgMaterialIssue issue = new MfgMaterialIssue();
        issue.issueNo = StringUtils.hasText(request.issueNo()) ? request.issueNo() : nextIssueNo();
        issue.taskId = request.taskId();
        issue.warehouseId = request.warehouseId();
        issue.issueUserName = request.issueUserName();
        issue.issueTime = request.issueTime() == null ? LocalDateTime.now() : request.issueTime();
        issue.status = "DRAFT";
        issue.remark = request.remark();
        save(issue);

        for (MfgMaterialIssueCreateRequest.Item source : request.items()) {
            MfgMaterialIssueItem item = new MfgMaterialIssueItem();
            item.issueId = issue.id;
            item.taskId = request.taskId();
            item.materialId = source.materialId();
            item.materialName = StringUtils.hasText(source.materialName()) ? source.materialName() : "物料-" + source.materialId();
            item.warehouseId = source.warehouseId() == null ? request.warehouseId() : source.warehouseId();
            item.warehouseName = source.warehouseName();
            item.planQty = source.planQty() == null ? BigDecimal.ZERO : source.planQty();
            item.issueQty = source.issueQty();
            item.unitName = source.unitName();
            item.remark = source.remark();
            itemMapper.insert(item);
        }

        task.setMaterialStatus("PART_ISSUED");
        if ("DRAFT".equals(task.getStatus())) {
            task.setStatus("CONFIRMED");
        }
        taskService.updateById(task);
        return detail(issue.id);
    }

    @Transactional
    public MfgMaterialIssue changeStatus(Long id, String status) {
        if (!List.of("DRAFT", "CONFIRMED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("不支持的领料单状态: " + status);
        }
        MfgMaterialIssue issue = requiredIssue(id);
        issue.status = status;
        updateById(issue);
        if ("CONFIRMED".equals(status)) {
            MfgTask task = taskService.requiredTask(issue.taskId);
            task.setMaterialStatus("ISSUED");
            taskService.updateById(task);
        }
        return requiredIssue(id);
    }

    @Transactional
    public void removeIssue(Long id) {
        MfgMaterialIssue issue = requiredIssue(id);
        if (!List.of("DRAFT", "CANCELLED").contains(issue.status)) {
            throw new IllegalStateException("只有草稿或已作废的领料单可以删除");
        }
        removeById(id);
        itemMapper.delete(new QueryWrapper<MfgMaterialIssueItem>().eq("issue_id", id));
    }

    private MfgMaterialIssue requiredIssue(Long id) {
        MfgMaterialIssue issue = getById(id);
        if (issue == null) {
            throw new IllegalArgumentException("生产领料单不存在");
        }
        return issue;
    }

    private List<MfgMaterialIssueItem> items(Long issueId) {
        return itemMapper.selectList(new QueryWrapper<MfgMaterialIssueItem>()
            .eq("issue_id", issueId)
            .orderByAsc("id"));
    }

    private String nextIssueNo() {
        return "SCLL" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }
}
