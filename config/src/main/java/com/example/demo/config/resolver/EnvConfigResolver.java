package com.example.demo.config.resolver;

import com.example.demo.config.api.enums.ConfigValueType;
import com.example.demo.config.config.ConfigConstants;
import com.example.demo.config.support.ConfigCryptoService;
import com.example.demo.config.support.ConfigKey;
import com.example.demo.config.support.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * 环境变量配置解析器。
 */
public class EnvConfigResolver implements ConfigResolver {

    private final Environment environment;
    private final ConfigConstants constants;
    private final ConfigCryptoService cryptoService;

    public EnvConfigResolver(Environment environment,
                             ConfigConstants constants,
                             ConfigCryptoService cryptoService) {
        this.environment = environment;
        this.constants = constants;
        this.cryptoService = cryptoService;
    }

    @Override
    public ConfigValue resolve(ConfigResolveRequest request) {
        if (request == null || request.getKey() == null) {
            return null;
        }
        ConfigKey key = request.getKey();
        String group = normalizeGroup(key.getGroup());
        String propertyKey = "config." + group + "." + key.getKey();
        String value = environment.getProperty(propertyKey);
        if (value == null) {
            value = environment.getProperty("config." + key.getKey());
        }
        if (value == null) {
            return null;
        }
        String resolved = maybeDecrypt(value);
        ConfigValueType type = guessType(resolved);
        return new ConfigValue(group, key.getKey(), resolved, type, null, true);
    }

    private String normalizeGroup(String group) {
        String fallback = constants.getGroup().getDefaultGroup();
        return StringUtils.defaultIfBlank(group, fallback);
    }

    private String maybeDecrypt(String value) {
        if (cryptoService.isEncrypted(value)) {
            return cryptoService.decryptIfNeeded(true, value);
        }
        return value;
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
