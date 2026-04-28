package com.yize.erp.masterdata.dto;

import java.math.BigDecimal;

public record CustomerSaveRequest(
        String customerCode,
        String customerName,
        Long categoryId,
        String contactName,
        String mobile,
        String telephone,
        String email,
        String country,
        String address,
        String salespersonName,
        String logisticsCompany,
        String linkedSupplier,
        BigDecimal prepaidAmount,
        String memberLevel,
        Integer selfOrderEnabled,
        String loginAccount,
        BigDecimal creditLimit,
        Integer invoiceEnabled,
        Integer status,
        String tags,
        String remark
) {}
