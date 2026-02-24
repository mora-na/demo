package com.example.demo.config.api.enums;

import java.util.Locale;

/**
 * 配置值类型。
 */
public enum ConfigValueType {
    STRING,
    NUMBER,
    BOOLEAN,
    JSON;

    public static ConfigValueType from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        for (ConfigValueType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return null;
    }

    public boolean isJson() {
        return this == JSON;
    }
}
