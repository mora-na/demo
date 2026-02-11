package com.example.demo.common.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存存储实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class RedisCacheStore implements CacheStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisCacheStore(RedisTemplate<String, Object> redisTemplate,
                           StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        if (key == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return value;
        }
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        if (key == null) {
            return false;
        }
        if (value instanceof String) {
            return stringRedisTemplate.opsForValue().setIfAbsent(key, (String) value, ttl);
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    @Override
    public Long increment(String key) {
        if (key == null) {
            return null;
        }
        return stringRedisTemplate.opsForValue().increment(key);
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        Boolean deleted = stringRedisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    @Override
    public boolean hasKey(String key) {
        if (key == null) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        if (key == null || ttl == null) {
            return false;
        }
        Boolean updated = stringRedisTemplate.expire(key, ttl);
        return Boolean.TRUE.equals(updated);
    }

    @Override
    public long getExpire(String key, TimeUnit unit) {
        if (key == null) {
            return -2;
        }
        Long ttl = stringRedisTemplate.getExpire(key, unit);
        return ttl == null ? -2 : ttl;
    }
}
