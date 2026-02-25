package com.example.demo.config.support;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.config.config.ConfigConstants;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 配置缓存服务。
 */
@Component
public class ConfigCacheService {

    private final CacheTool cacheTool;
    private final ConfigCacheKeyBuilder keyBuilder;
    private final ConfigConstants constants;

    public ConfigCacheService(CacheTool cacheTool, ConfigConstants constants) {
        this.cacheTool = cacheTool;
        this.constants = constants;
        this.keyBuilder = new ConfigCacheKeyBuilder(constants);
    }

    public ConfigCacheValue get(String group, String key) {
        return getResult(group, key).getValue();
    }

    public ConfigCacheResult getResult(String group, String key) {
        Object value = cacheTool.get(keyBuilder.build(group, key));
        if (value instanceof ConfigCacheValue) {
            return ConfigCacheResult.hit((ConfigCacheValue) value);
        }
        if (value instanceof ConfigCacheMiss) {
            return ConfigCacheResult.miss();
        }
        return ConfigCacheResult.empty();
    }

    public void put(ConfigCacheValue value) {
        if (value == null) {
            return;
        }
        Duration ttl = resolveTtl();
        if (ttl == null) {
            return;
        }
        cacheTool.set(keyBuilder.build(value.getGroup(), value.getKey()), value, ttl);
    }

    public void putAll(List<ConfigCacheValue> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (ConfigCacheValue value : values) {
            put(value);
        }
    }

    public void putMiss(String group, String key) {
        Duration ttl = resolveMissTtl();
        if (ttl == null) {
            return;
        }
        cacheTool.set(keyBuilder.build(group, key), ConfigCacheMiss.INSTANCE, ttl);
    }

    public void putMisses(String group, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        Duration ttl = resolveMissTtl();
        if (ttl == null) {
            return;
        }
        for (String key : keys) {
            cacheTool.set(keyBuilder.build(group, key), ConfigCacheMiss.INSTANCE, ttl);
        }
    }

    public void evict(String group, String key) {
        cacheTool.delete(keyBuilder.build(group, key));
    }

    private Duration resolveTtl() {
        long seconds = constants.getCache().getTtlSeconds();
        if (seconds <= 0) {
            return null;
        }
        return Duration.ofSeconds(seconds);
    }

    private Duration resolveMissTtl() {
        long seconds = constants.getCache().getMissTtlSeconds();
        if (seconds <= 0) {
            return null;
        }
        return Duration.ofSeconds(seconds);
    }
}
