package com.yize.erp.manufacturing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MfgMaterialIssueCreateRequest(
    String issueNo,
    @NotNull Long taskId,
    @NotNull Long warehouseId,
    String issueUserName,
    LocalDateTime issueTime,
    String remark,
    @Valid @NotEmpty List<Item> items
) {
    public record Item(
        @NotNull Long materialId,
        String materialName,
        Long warehouseId,
        String warehouseName,
        BigDecimal planQty,
        @NotNull @Positive BigDecimal issueQty,
        String unitName,
        String remark
    ) {
    }
}
