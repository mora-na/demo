package com.example.demo.common.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类，委托给配置的缓存存储实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class CacheTool {

    private final CacheStore cacheStore;

    public CacheTool(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    public void set(String key, Object value, Duration ttl) {
        cacheStore.set(key, value, ttl);
    }

    public Object get(String key) {
        return cacheStore.get(key);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = cacheStore.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        return cacheStore.setIfAbsent(key, value, ttl);
    }

    public Long increment(String key) {
        return cacheStore.increment(key);
    }

    public boolean delete(String key) {
        return cacheStore.delete(key);
    }

    public boolean hasKey(String key) {
        return cacheStore.hasKey(key);
    }

    public boolean expire(String key, long ttl, TimeUnit unit) {
        if (ttl <= 0) {
            return false;
        }
        return cacheStore.expire(key, Duration.ofMillis(unit.toMillis(ttl)));
    }

    public boolean expire(String key, Duration ttl) {
        return cacheStore.expire(key, ttl);
    }

    public long getExpire(String key, TimeUnit unit) {
        return cacheStore.getExpire(key, unit);
    }
}
