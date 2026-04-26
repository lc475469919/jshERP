package com.yize.erp.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;

public record MfgTaskStatusRequest(
    @NotBlank String status
) {
}
