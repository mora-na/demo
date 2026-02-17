package com.example.demo.extension.model;

import java.util.Locale;

/**
 * 动态接口生命周期状态。
 */
public enum DynamicApiStatus {
    DRAFT,
    ENABLED,
    DISABLED,
    DELETED;

    public static DynamicApiStatus from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (DynamicApiStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        return null;
    }
}
