package com.yize.erp.system.dto;

public record NumberRuleSaveRequest(
        String documentType,
        String documentName,
        String prefix,
        String datePattern,
        Integer serialLength,
        Integer currentSerial,
        Integer resetDaily,
        Integer status,
        String remark
) {
}
