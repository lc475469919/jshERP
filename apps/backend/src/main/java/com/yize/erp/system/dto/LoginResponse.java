package com.yize.erp.system.dto;

import java.util.List;

public record LoginResponse(
        String tokenName,
        String tokenValue,
        UserProfile user,
        List<MenuNode> menus,
        List<String> permissions
) {
}
