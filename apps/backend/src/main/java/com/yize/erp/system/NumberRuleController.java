package com.yize.erp.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.system.dto.NumberRuleSaveRequest;
import com.yize.erp.system.entity.SysNumberRule;
import com.yize.erp.system.log.LogOperation;
import com.yize.erp.system.mapper.SysNumberRuleMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/system/number-rules")
public class NumberRuleController {
    private final SysNumberRuleMapper numberRuleMapper;

    public NumberRuleController(SysNumberRuleMapper numberRuleMapper) {
        this.numberRuleMapper = numberRuleMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<SysNumberRule>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        LambdaQueryWrapper<SysNumberRule> query = new LambdaQueryWrapper<SysNumberRule>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(SysNumberRule::getDocumentType, keyword)
                        .or()
                        .like(SysNumberRule::getDocumentName, keyword))
                .orderByDesc(SysNumberRule::getId);
        Page<SysNumberRule> result = numberRuleMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping
    @LogOperation(module = "编号规则", operation = "新增编号规则")
    public ApiResponse<Void> create(@RequestBody NumberRuleSaveRequest request) {
        SysNumberRule rule = new SysNumberRule();
        copy(rule, request);
        numberRuleMapper.insert(rule);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @LogOperation(module = "编号规则", operation = "编辑编号规则")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody NumberRuleSaveRequest request) {
        SysNumberRule rule = numberRuleMapper.selectById(id);
        if (rule == null) {
            throw new IllegalArgumentException("编号规则不存在");
        }
        copy(rule, request);
        numberRuleMapper.updateById(rule);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @LogOperation(module = "编号规则", operation = "删除编号规则")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        numberRuleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/preview")
    public ApiResponse<String> preview(@PathVariable Long id) {
        SysNumberRule rule = numberRuleMapper.selectById(id);
        if (rule == null) {
            throw new IllegalArgumentException("编号规则不存在");
        }
        String dateText = LocalDate.now().format(DateTimeFormatter.ofPattern(rule.getDatePattern()));
        int nextSerial = SystemHelper.zero(rule.getCurrentSerial()) + 1;
        return ApiResponse.ok(rule.getPrefix() + "-" + dateText + "-" + String.format("%0" + rule.getSerialLength() + "d", nextSerial));
    }

    private void copy(SysNumberRule rule, NumberRuleSaveRequest request) {
        rule.setDocumentType(SystemHelper.requireText(request.documentType(), "单据类型不能为空"));
        rule.setDocumentName(SystemHelper.requireText(request.documentName(), "单据名称不能为空"));
        rule.setPrefix(SystemHelper.requireText(request.prefix(), "前缀不能为空"));
        rule.setDatePattern(request.datePattern() == null || request.datePattern().isBlank() ? "yyyyMMdd" : request.datePattern());
        rule.setSerialLength(request.serialLength() == null ? 4 : request.serialLength());
        rule.setCurrentSerial(SystemHelper.zero(request.currentSerial()));
        rule.setResetDaily(SystemHelper.enabled(request.resetDaily()));
        rule.setStatus(SystemHelper.enabled(request.status()));
        rule.setRemark(request.remark());
    }
}
