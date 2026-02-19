package com.example.demo.extension.api.executor;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动态接口配置校验结果。
 */
@Getter
public class ConfigValidationResult {

    private final boolean valid;
    private final String messageKey;
    private final Map<String, String> fieldErrors;

    private ConfigValidationResult(boolean valid, String messageKey, Map<String, String> fieldErrors) {
        this.valid = valid;
        this.messageKey = messageKey;
        this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : Collections.unmodifiableMap(fieldErrors);
    }

    public static ConfigValidationResult ok() {
        return new ConfigValidationResult(true, null, Collections.emptyMap());
    }

    public static ConfigValidationResult error(String messageKey) {
        return new ConfigValidationResult(false, messageKey, Collections.emptyMap());
    }

    public static ConfigValidationResult error(String messageKey, Map<String, String> fieldErrors) {
        Map<String, String> errors = fieldErrors == null ? null : new LinkedHashMap<>(fieldErrors);
        return new ConfigValidationResult(false, messageKey, errors);
    }
}
