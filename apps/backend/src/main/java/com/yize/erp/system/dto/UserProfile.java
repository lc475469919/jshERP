package com.yize.erp.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfile(
        Long id,
        String account,
        String name,
        String mobile,
        Integer status,
        LocalDateTime lastLoginAt,
        List<String> roles
) {
}
