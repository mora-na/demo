package com.example.demo.config.support;

import com.example.demo.config.api.enums.ConfigValueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 配置值解析器。
 */
@Component
public class ConfigValueParser {

    private final ObjectMapper objectMapper;

    public ConfigValueParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String asString(ConfigValue value, String defaultValue) {
        if (value == null || value.getValue() == null) {
            return defaultValue;
        }
        return value.getValue();
    }

    public Boolean asBoolean(ConfigValue value, Boolean defaultValue) {
        if (value == null || value.getValue() == null) {
            return defaultValue;
        }
        String raw = value.getValue().trim();
        if (raw.isEmpty()) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        return defaultValue;
    }

    public Integer asInteger(ConfigValue value, Integer defaultValue) {
        if (value == null || value.getValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.getValue().trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public Long asLong(ConfigValue value, Long defaultValue) {
        if (value == null || value.getValue() == null) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value.getValue().trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public BigDecimal asDecimal(ConfigValue value, BigDecimal defaultValue) {
        if (value == null || value.getValue() == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.getValue().trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public <T> T asObject(ConfigValue value, Class<T> type, T defaultValue) {
        if (value == null || value.getValue() == null || type == null) {
            return defaultValue;
        }
        String raw = value.getValue();
        if (type == String.class) {
            return type.cast(raw);
        }
        if (type == Integer.class) {
            return type.cast(asInteger(value, null));
        }
        if (type == Long.class) {
            return type.cast(asLong(value, null));
        }
        if (type == Boolean.class) {
            return type.cast(asBoolean(value, null));
        }
        if (type == BigDecimal.class) {
            return type.cast(asDecimal(value, null));
        }
        if (value.getType() == ConfigValueType.JSON || looksLikeJson(raw)) {
            try {
                return objectMapper.readValue(raw, type);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        try {
            return objectMapper.convertValue(raw, type);
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    private boolean looksLikeJson(String raw) {
        if (StringUtils.isBlank(raw)) {
            return false;
        }
        String trimmed = raw.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}
