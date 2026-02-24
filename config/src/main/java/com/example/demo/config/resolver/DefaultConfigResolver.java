package com.example.demo.config.resolver;

import com.example.demo.config.api.enums.ConfigValueType;
import com.example.demo.config.config.ConfigConstants;
import com.example.demo.config.config.ConfigDefaultsProperties;
import com.example.demo.config.support.ConfigKey;
import com.example.demo.config.support.ConfigValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 默认值配置解析器。
 */
public class DefaultConfigResolver implements ConfigResolver {

    private final ConfigDefaultsProperties defaultsProperties;
    private final ConfigConstants constants;

    public DefaultConfigResolver(ConfigDefaultsProperties defaultsProperties,
                                 ConfigConstants constants) {
        this.defaultsProperties = defaultsProperties;
        this.constants = constants;
    }

    @Override
    public ConfigValue resolve(ConfigResolveRequest request) {
        if (request == null || request.getKey() == null) {
            return null;
        }
        ConfigKey key = request.getKey();
        String group = normalizeGroup(key.getGroup());
        String value = lookup(group, key.getKey());
        if (value == null) {
            return null;
        }
        ConfigValueType type = guessType(value);
        return new ConfigValue(group, key.getKey(), value, type, 0, false);
    }

    private String normalizeGroup(String group) {
        String fallback = constants.getGroup().getDefaultGroup();
        return StringUtils.defaultIfBlank(group, fallback);
    }

    private String lookup(String group, String key) {
        if (defaultsProperties == null) {
            return null;
        }
        Map<String, String> items = defaultsProperties.getItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        String value = items.get(group + "." + key);
        if (value != null) {
            return value;
        }
        return items.get(key);
    }

    private ConfigValueType guessType(String value) {
        if (StringUtils.isBlank(value)) {
            return ConfigValueType.STRING;
        }
        String trimmed = value.trim();
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return ConfigValueType.BOOLEAN;
        }
        if (looksLikeJson(trimmed)) {
            return ConfigValueType.JSON;
        }
        if (isNumber(trimmed)) {
            return ConfigValueType.NUMBER;
        }
        return ConfigValueType.STRING;
    }

    private boolean looksLikeJson(String value) {
        return (value.startsWith("{") && value.endsWith("}"))
                || (value.startsWith("[") && value.endsWith("]"));
    }

    private boolean isNumber(String value) {
        try {
            new java.math.BigDecimal(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
