package com.example.demo.config.resolver;

import com.example.demo.config.support.ConfigKey;

/**
 * 配置解析请求。
 */
public class ConfigResolveRequest {

    private final ConfigKey key;

    public ConfigResolveRequest(ConfigKey key) {
        this.key = key;
    }

    public ConfigKey getKey() {
        return key;
    }
}
