package com.example.demo.config.service.impl;

import com.example.demo.config.resolver.ConfigResolveRequest;
import com.example.demo.config.resolver.ConfigResolverChain;
import com.example.demo.config.service.ConfigQueryService;
import com.example.demo.config.support.ConfigKey;
import com.example.demo.config.support.ConfigValue;
import com.example.demo.config.support.ConfigValueParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 配置读取服务实现。
 */
@Service
public class ConfigQueryServiceImpl implements ConfigQueryService {

    private final ConfigResolverChain resolverChain;
    private final ConfigValueParser valueParser;

    public ConfigQueryServiceImpl(ConfigResolverChain resolverChain, ConfigValueParser valueParser) {
        this.resolverChain = resolverChain;
        this.valueParser = valueParser;
    }

    @Override
    public ConfigValue resolve(String group, String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        ConfigKey configKey = new ConfigKey(group, key.trim());
        return resolverChain.resolve(new ConfigResolveRequest(configKey));
    }

    @Override
    public String getString(String group, String key, String defaultValue) {
        return valueParser.asString(resolve(group, key), defaultValue);
    }

    @Override
    public Boolean getBoolean(String group, String key, Boolean defaultValue) {
        return valueParser.asBoolean(resolve(group, key), defaultValue);
    }

    @Override
    public Integer getInteger(String group, String key, Integer defaultValue) {
        return valueParser.asInteger(resolve(group, key), defaultValue);
    }

    @Override
    public Long getLong(String group, String key, Long defaultValue) {
        return valueParser.asLong(resolve(group, key), defaultValue);
    }

    @Override
    public <T> T getObject(String group, String key, Class<T> type, T defaultValue) {
        return valueParser.asObject(resolve(group, key), type, defaultValue);
    }
}
