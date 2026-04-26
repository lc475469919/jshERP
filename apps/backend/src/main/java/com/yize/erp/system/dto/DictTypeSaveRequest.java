package com.yize.erp.system.dto;

public record DictTypeSaveRequest(
        String dictCode,
        String dictName,
        Integer status,
        String remark
) {
}
