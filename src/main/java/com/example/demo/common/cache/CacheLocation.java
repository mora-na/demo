package com.example.demo.common.cache;

/**
 * 缓存后端位置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public enum CacheLocation {
    REDIS,
    MEMORY,
    DB;

    /**
     * 解析缓存位置文本，默认返回 REDIS。
     *
     * @param value 原始文本
     * @return 解析结果
     */
    public static CacheLocation from(String value) {
        if (value == null) {
            return REDIS;
        }
        String normalized = value.trim().toUpperCase();
        for (CacheLocation location : values()) {
            if (location.name().equals(normalized)) {
                return location;
            }
        }
        return REDIS;
    }
}
