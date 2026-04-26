package com.yize.erp.system;

public final class SystemHelper {
    private SystemHelper() {
    }

    public static Integer enabled(Integer value) {
        return value == null ? 1 : value;
    }

    public static Integer zero(Integer value) {
        return value == null ? 0 : value;
    }

    public static Long root(Long value) {
        return value == null ? 0L : value;
    }

    public static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
