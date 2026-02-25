package com.example.demo.config.support;

import java.io.Serializable;

/**
 * 配置负缓存占位对象。
 */
public class ConfigCacheMiss implements Serializable {

    public static final ConfigCacheMiss INSTANCE = new ConfigCacheMiss();
    private static final long serialVersionUID = 1L;
    private final String marker = "MISS";

    public ConfigCacheMiss() {
    }

    public String getMarker() {
        return marker;
    }
}
