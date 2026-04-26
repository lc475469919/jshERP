package com.yize.erp.system.dto;

public record DictItemSaveRequest(
        Long dictTypeId,
        String itemLabel,
        String itemValue,
        String color,
        Integer sortOrder,
        Integer status,
        String remark
) {
}
