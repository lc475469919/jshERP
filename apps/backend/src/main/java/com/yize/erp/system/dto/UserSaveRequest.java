package com.yize.erp.system.dto;

import java.util.List;

public record UserSaveRequest(
        String account,
        String name,
        String mobile,
        String password,
        Integer status,
        String remark,
        List<Long> roleIds
) {
}
