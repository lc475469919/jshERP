package com.yize.erp.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MfgTaskCreateRequest(
    String taskNo,
    @NotNull LocalDate taskDate,
    String priority,
    String salesOrderNo,
    Long customerId,
    String customerName,
    String customerOrderNo,
    @NotNull Long productId,
    @NotBlank String productName,
    BigDecimal orderedQty,
    @NotNull @Positive BigDecimal planQty,
    LocalDate planStartDate,
    LocalDate planFinishDate,
    String marker,
    String remark
) {
}
