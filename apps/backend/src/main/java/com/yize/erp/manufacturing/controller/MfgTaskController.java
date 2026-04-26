package com.yize.erp.manufacturing.controller;

import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.manufacturing.domain.MfgTask;
import com.yize.erp.manufacturing.dto.MfgTaskCreateRequest;
import com.yize.erp.manufacturing.dto.MfgTaskQueryRequest;
import com.yize.erp.manufacturing.dto.MfgTaskStatusRequest;
import com.yize.erp.manufacturing.dto.MfgTaskUpdateRequest;
import com.yize.erp.manufacturing.service.MfgTaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manufacturing/tasks")
public class MfgTaskController {
    private final MfgTaskService taskService;

    public MfgTaskController(MfgTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ApiResponse<PageResult<MfgTask>> page(
        @RequestParam(required = false) Long pageNo,
        @RequestParam(required = false) Long pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.ok(taskService.pageTasks(new MfgTaskQueryRequest(pageNo, pageSize, keyword, status, startDate, endDate)));
    }

    @GetMapping("/{id}")
    public ApiResponse<MfgTask> detail(@PathVariable Long id) {
        return ApiResponse.ok(taskService.requiredTask(id));
    }

    @PostMapping
    public ApiResponse<MfgTask> create(@Valid @RequestBody MfgTaskCreateRequest request) {
        return ApiResponse.ok(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MfgTask> update(@PathVariable Long id, @Valid @RequestBody MfgTaskUpdateRequest request) {
        return ApiResponse.ok(taskService.updateTask(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<MfgTask> changeStatus(@PathVariable Long id, @Valid @RequestBody MfgTaskStatusRequest request) {
        return ApiResponse.ok(taskService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        taskService.removeTask(id);
        return ApiResponse.ok();
    }
}
