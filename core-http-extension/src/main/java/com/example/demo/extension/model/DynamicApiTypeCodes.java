package com.example.demo.extension.model;

import java.util.Locale;

/**
 * 动态接口类型编码。
 */
public final class DynamicApiTypeCodes {

    public static final String BEAN = "BEAN";
    public static final String SQL = "SQL";
    public static final String HTTP = "HTTP";

    private DynamicApiTypeCodes() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    public static boolean isBean(String value) {
        return BEAN.equals(normalize(value));
    }

    public static boolean isSql(String value) {
        return SQL.equals(normalize(value));
    }

    public static boolean isHttp(String value) {
        return HTTP.equals(normalize(value));
    }
}
