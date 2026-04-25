package com.yize.erp.manufacturing.dto;

public record MfgMaterialIssueQueryRequest(
    Long pageNo,
    Long pageSize,
    String keyword,
    Long taskId,
    String status
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
