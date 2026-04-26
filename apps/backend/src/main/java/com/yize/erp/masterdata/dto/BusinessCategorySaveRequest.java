package com.yize.erp.masterdata.dto;

public record BusinessCategorySaveRequest(String categoryType, Long parentId, String categoryCode, String categoryName, Integer sortOrder, Integer saleEnabled, Integer purchaseEnabled) {}
