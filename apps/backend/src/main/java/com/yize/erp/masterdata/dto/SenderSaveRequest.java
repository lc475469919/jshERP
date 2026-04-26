package com.yize.erp.masterdata.dto;

public record SenderSaveRequest(
        Long logisticsId,
        String senderName,
        String phone,
        String address,
        Integer defaultFlag
) {}
