package com.yize.erp.masterdata.dto;

import java.math.BigDecimal;

public record ProductSaveRequest(
        String productCode,
        String productName,
        String mnemonicCode,
        Long categoryId,
        String barcode,
        Long brandId,
        String imageUrl,
        String specification,
        Long supplierId,
        String shelfNo,
        Long defaultUnitId,
        BigDecimal purchasePrice,
        BigDecimal costPrice,
        BigDecimal wholesalePrice,
        BigDecimal retailPrice,
        BigDecimal purchaseTaxRate,
        BigDecimal saleTaxRate,
        BigDecimal minStock,
        BigDecimal maxStock,
        String colorNames,
        Integer serialEnabled,
        Integer batchEnabled,
        Integer expiryEnabled,
        Integer saleEnabled,
        Integer purchaseEnabled,
        Integer selfMadeEnabled,
        Integer outsourcingEnabled,
        Integer status,
        String remark,
        String detailDescription,
        String productionDepartment
) {}
