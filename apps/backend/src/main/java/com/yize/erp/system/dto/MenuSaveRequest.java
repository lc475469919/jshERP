package com.yize.erp.system.dto;

public record MenuSaveRequest(
        Long parentId,
        String menuType,
        String menuName,
        String routePath,
        String componentPath,
        String permissionCode,
        String icon,
        Integer sortOrder,
        Integer visible,
        Integer status
) {
}
