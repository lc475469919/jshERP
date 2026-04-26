package com.yize.erp.common;

import java.util.List;

public record PageResult<T>(long total, long page, long pageSize, List<T> records) {
}
