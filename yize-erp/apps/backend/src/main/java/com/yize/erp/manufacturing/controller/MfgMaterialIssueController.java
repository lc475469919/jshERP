package com.yize.erp.manufacturing.controller;

import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.manufacturing.domain.MfgMaterialIssue;
import com.yize.erp.manufacturing.dto.MfgMaterialIssueCreateRequest;
import com.yize.erp.manufacturing.dto.MfgMaterialIssueQueryRequest;
import com.yize.erp.manufacturing.dto.MfgMaterialIssueResponse;
import com.yize.erp.manufacturing.dto.MfgTaskStatusRequest;
import com.yize.erp.manufacturing.service.MfgMaterialIssueService;
import jakarta.validation.Valid;
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
@RequestMapping("/manufacturing/material-issues")
public class MfgMaterialIssueController {
    private final MfgMaterialIssueService issueService;

    public MfgMaterialIssueController(MfgMaterialIssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ApiResponse<PageResult<MfgMaterialIssue>> page(
        @RequestParam(required = false) Long pageNo,
        @RequestParam(required = false) Long pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long taskId,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(issueService.pageIssues(new MfgMaterialIssueQueryRequest(pageNo, pageSize, keyword, taskId, status)));
    }

    @GetMapping("/{id}")
    public ApiResponse<MfgMaterialIssueResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(issueService.detail(id));
    }

    @PostMapping
    public ApiResponse<MfgMaterialIssueResponse> create(@Valid @RequestBody MfgMaterialIssueCreateRequest request) {
        return ApiResponse.ok(issueService.createIssue(request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<MfgMaterialIssue> changeStatus(@PathVariable Long id, @Valid @RequestBody MfgTaskStatusRequest request) {
        return ApiResponse.ok(issueService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        issueService.removeIssue(id);
        return ApiResponse.ok();
    }
}
