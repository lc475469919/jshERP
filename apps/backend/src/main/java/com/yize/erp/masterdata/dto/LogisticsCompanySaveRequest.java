package com.yize.erp.masterdata.dto;

public record LogisticsCompanySaveRequest(
        String logisticsCode,
        String logisticsName,
        String expressType,
        String expressCompanyName,
        String remark
) {}
