package com.example.demo.extension.model;

import java.util.Locale;

/**
 * 动态接口执行类型。
 */
public enum DynamicApiType {
    BEAN,
    SQL,
    HTTP;

    public static DynamicApiType from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (DynamicApiType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
