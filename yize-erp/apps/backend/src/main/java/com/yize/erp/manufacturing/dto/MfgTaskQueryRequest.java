package com.yize.erp.manufacturing.dto;

import java.time.LocalDate;

public record MfgTaskQueryRequest(
    Long pageNo,
    Long pageSize,
    String keyword,
    String status,
    LocalDate startDate,
    LocalDate endDate
) {
    public long safePageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    public long safePageSize() {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
