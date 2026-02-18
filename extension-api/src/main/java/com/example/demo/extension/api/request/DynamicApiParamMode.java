package com.example.demo.extension.api.request;

import java.util.Locale;

/**
 * 动态接口参数模式。
 */
public enum DynamicApiParamMode {
    AUTO,
    QUERY,
    BODY_JSON,
    FORM,
    MULTIPART;

    public static DynamicApiParamMode from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (DynamicApiParamMode mode : values()) {
            if (mode.name().equals(normalized)) {
                return mode;
            }
        }
        return null;
    }
}
