package com.yize.erp.masterdata.dto;

public record ProductAttrSaveRequest(String attrType, String attrName, Integer allowDecimal, Integer sortOrder, String remark) {}
