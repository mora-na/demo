package com.example.demo.job.config;

import java.util.Locale;

/**
 * Coverage mode for async log probes.
 */
public enum AsyncLogCoverageMode {

    BASELINE("baseline"),
    BRANCHES("branches");

    private final String value;

    AsyncLogCoverageMode(String value) {
        this.value = value;
    }

    public static AsyncLogCoverageMode from(String value) {
        if (value == null) {
            return BASELINE;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (AsyncLogCoverageMode mode : values()) {
            if (mode.value.equals(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unsupported async log coverage mode: " + value);
    }

    public String getValue() {
        return value;
    }
}
