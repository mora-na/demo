package com.example.demo.config.support;

/**
 * 配置缓存查询结果。
 */
public class ConfigCacheResult {

    private final ConfigCacheValue value;
    private final boolean miss;

    private ConfigCacheResult(ConfigCacheValue value, boolean miss) {
        this.value = value;
        this.miss = miss;
    }

    public static ConfigCacheResult hit(ConfigCacheValue value) {
        return new ConfigCacheResult(value, false);
    }

    public static ConfigCacheResult miss() {
        return new ConfigCacheResult(null, true);
    }

    public static ConfigCacheResult empty() {
        return new ConfigCacheResult(null, false);
    }

    public boolean isHit() {
        return value != null;
    }

    public boolean isMiss() {
        return miss;
    }

    public ConfigCacheValue getValue() {
        return value;
    }
}
