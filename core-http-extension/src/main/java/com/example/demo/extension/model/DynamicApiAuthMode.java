package com.example.demo.extension.model;

import java.util.Locale;

/**
 * 动态接口认证模式。
 */
public enum DynamicApiAuthMode {
    INHERIT,
    PUBLIC;

    public static DynamicApiAuthMode from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (DynamicApiAuthMode mode : values()) {
            if (mode.name().equals(normalized)) {
                return mode;
            }
        }
        return null;
    }
}
