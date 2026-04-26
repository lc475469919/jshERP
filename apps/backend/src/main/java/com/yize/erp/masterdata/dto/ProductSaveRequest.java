package com.yize.erp.masterdata.dto;

import java.math.BigDecimal;

public record ProductSaveRequest(
        String productCode,
        String productName,
        String mnemonicCode,
        Long categoryId,
        String barcode,
        Long brandId,
        String specification,
        Long supplierId,
        String shelfNo,
        Long defaultUnitId,
        BigDecimal purchasePrice,
        BigDecimal costPrice,
        BigDecimal wholesalePrice,
        BigDecimal retailPrice,
        Integer status,
        String remark
) {}
