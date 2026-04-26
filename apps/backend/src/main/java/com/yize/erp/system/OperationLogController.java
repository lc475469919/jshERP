package com.yize.erp.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.system.entity.SysOperationLog;
import com.yize.erp.system.mapper.SysOperationLogMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/logs")
public class OperationLogController {
    private final SysOperationLogMapper operationLogMapper;

    public OperationLogController(SysOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        LambdaQueryWrapper<SysOperationLog> query = new LambdaQueryWrapper<SysOperationLog>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(SysOperationLog::getUserName, keyword)
                        .or()
                        .like(SysOperationLog::getModuleName, keyword)
                        .or()
                        .like(SysOperationLog::getBusinessNo, keyword))
                .orderByDesc(SysOperationLog::getId);
        Page<SysOperationLog> result = operationLogMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }
}
