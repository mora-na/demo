package com.example.demo.common.cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存存储抽象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface CacheStore {

    void set(String key, Object value, Duration ttl);

    Object get(String key);

    Boolean setIfAbsent(String key, Object value, Duration ttl);

    Long increment(String key);

    boolean delete(String key);

    boolean hasKey(String key);

    boolean expire(String key, Duration ttl);

    long getExpire(String key, TimeUnit unit);
}
