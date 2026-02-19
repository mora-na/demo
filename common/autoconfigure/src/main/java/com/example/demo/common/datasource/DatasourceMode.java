package com.example.demo.common.datasource;

import java.util.Locale;

/**
 * 数据源模式：
 * 1) multi-datasource: 多数据源（按模块/读写切换）
 * 2) single-datasource-multi-schema: 单数据源，多 schema
 * 3) single-datasource-single-schema: 单数据源，单 schema
 */
public enum DatasourceMode {
    MULTI_DATASOURCE("multi-datasource"),
    SINGLE_DATASOURCE_MULTI_SCHEMA("single-datasource-multi-schema"),
    SINGLE_DATASOURCE_SINGLE_SCHEMA("single-datasource-single-schema");

    private final String value;

    DatasourceMode(String value) {
        this.value = value;
    }

    public static DatasourceMode fromProperty(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return MULTI_DATASOURCE;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (DatasourceMode mode : values()) {
            if (mode.value.equals(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unsupported app.datasource.mode: " + raw);
    }

    public String value() {
        return value;
    }
}
