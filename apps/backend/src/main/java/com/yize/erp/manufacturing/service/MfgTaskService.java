package com.yize.erp.manufacturing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yize.erp.common.PageResult;
import com.yize.erp.manufacturing.domain.MfgTask;
import com.yize.erp.manufacturing.dto.MfgTaskCreateRequest;
import com.yize.erp.manufacturing.dto.MfgTaskQueryRequest;
import com.yize.erp.manufacturing.dto.MfgTaskUpdateRequest;
import com.yize.erp.manufacturing.mapper.MfgTaskMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MfgTaskService extends ServiceImpl<MfgTaskMapper, MfgTask> {
    private static final Set<String> ALLOWED_STATUS =
        Set.of("DRAFT", "CONFIRMED", "IN_PROGRESS", "FINISHED", "CANCELLED");

    public PageResult<MfgTask> pageTasks(MfgTaskQueryRequest query) {
        Page<MfgTask> page = new Page<>(query.safePageNo(), query.safePageSize());
        LambdaQueryWrapper<MfgTask> wrapper = new LambdaQueryWrapper<MfgTask>()
            .eq(StringUtils.hasText(query.status()), MfgTask::getStatus, query.status())
            .ge(query.startDate() != null, MfgTask::getTaskDate, query.startDate())
            .le(query.endDate() != null, MfgTask::getTaskDate, query.endDate())
            .and(StringUtils.hasText(query.keyword()), item -> item
                .like(MfgTask::getTaskNo, query.keyword())
                .or()
                .like(MfgTask::getProductName, query.keyword())
                .or()
                .like(MfgTask::getCustomerName, query.keyword()))
            .orderByDesc(MfgTask::getTaskDate)
            .orderByDesc(MfgTask::getId);
        Page<MfgTask> result = page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Transactional
    public MfgTask createTask(MfgTaskCreateRequest request) {
        MfgTask task = new MfgTask();
        task.setTaskNo(StringUtils.hasText(request.taskNo()) ? request.taskNo() : nextTaskNo());
        task.setFinishedQty(BigDecimal.ZERO);
        task.setScrapQty(BigDecimal.ZERO);
        task.setMaterialStatus("NOT_ISSUED");
        task.setProcessStatus("NOT_STARTED");
        task.setStockInStatus("NOT_STOCKED");
        task.setStatus("DRAFT");
        applyCreateFields(task, request);
        save(task);
        return getById(task.getId());
    }

    @Transactional
    public MfgTask updateTask(Long id, MfgTaskUpdateRequest request) {
        MfgTask task = requiredTask(id);
        if (!Set.of("DRAFT", "CONFIRMED").contains(task.getStatus())) {
            throw new IllegalStateException("只有草稿或已确认的生产任务可以编辑");
        }
        applyUpdateFields(task, request);
        updateById(task);
        return getById(id);
    }

    @Transactional
    public MfgTask changeStatus(Long id, String status) {
        if (!ALLOWED_STATUS.contains(status)) {
            throw new IllegalArgumentException("不支持的生产任务状态: " + status);
        }
        MfgTask task = requiredTask(id);
        task.setStatus(status);
        if ("IN_PROGRESS".equals(status)) {
            task.setProcessStatus("IN_PROGRESS");
        }
        if ("FINISHED".equals(status)) {
            task.setProcessStatus("FINISHED");
            task.setActualFinishDate(LocalDate.now());
        }
        updateById(task);
        return getById(id);
    }

    @Transactional
    public void removeTask(Long id) {
        MfgTask task = requiredTask(id);
        if (!Set.of("DRAFT", "CANCELLED").contains(task.getStatus())) {
            throw new IllegalStateException("只有草稿或已作废的生产任务可以删除");
        }
        removeById(id);
    }

    public MfgTask requiredTask(Long id) {
        MfgTask task = getById(id);
        if (task == null) {
            throw new IllegalArgumentException("生产任务不存在");
        }
        return task;
    }

    private void applyCreateFields(MfgTask task, MfgTaskCreateRequest request) {
        task.setTaskDate(request.taskDate());
        task.setPriority(defaultText(request.priority(), "NORMAL"));
        task.setSalesOrderNo(request.salesOrderNo());
        task.setCustomerId(request.customerId());
        task.setCustomerName(request.customerName());
        task.setCustomerOrderNo(request.customerOrderNo());
        task.setProductId(request.productId());
        task.setProductName(request.productName());
        task.setOrderedQty(defaultAmount(request.orderedQty()));
        task.setPlanQty(request.planQty());
        task.setPlanStartDate(request.planStartDate());
        task.setPlanFinishDate(request.planFinishDate());
        task.setMarker(request.marker());
        task.setRemark(request.remark());
    }

    private void applyUpdateFields(MfgTask task, MfgTaskUpdateRequest request) {
        task.setTaskDate(request.taskDate());
        task.setPriority(defaultText(request.priority(), "NORMAL"));
        task.setSalesOrderNo(request.salesOrderNo());
        task.setCustomerId(request.customerId());
        task.setCustomerName(request.customerName());
        task.setCustomerOrderNo(request.customerOrderNo());
        task.setProductId(request.productId());
        task.setProductName(request.productName());
        task.setOrderedQty(defaultAmount(request.orderedQty()));
        task.setPlanQty(request.planQty());
        task.setPlanStartDate(request.planStartDate());
        task.setPlanFinishDate(request.planFinishDate());
        task.setMarker(request.marker());
        task.setRemark(request.remark());
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String nextTaskNo() {
        return "SCT" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + System.currentTimeMillis() % 100000;
    }
}
