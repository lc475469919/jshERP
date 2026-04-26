package com.yize.erp.masterdata.dto;

import java.math.BigDecimal;

public record SupplierSaveRequest(
        String supplierCode,
        String supplierName,
        Long categoryId,
        String contactName,
        String mobile,
        String email,
        String telephone,
        String address,
        BigDecimal prepaidAmount,
        Integer invoiceEnabled,
        Integer status,
        String remark
) {}
