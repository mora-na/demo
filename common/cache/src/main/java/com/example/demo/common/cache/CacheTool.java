package com.example.demo.common.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存工具类，委托给配置的缓存存储实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class CacheTool {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final Duration DEFAULT_LOCK_TTL = Duration.ofSeconds(30);

    private final CacheStore cacheStore;

    public CacheTool(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    public void set(String key, Object value, Duration ttl) {
        cacheStore.set(key, value, ttl);
    }

    public Boolean set(String key, Object value) {
        return cacheStore.set(key, value);
    }

    public Boolean setNx(String key, Object value) {
        return cacheStore.setNx(key, value);
    }

    public Boolean setXx(String key, Object value) {
        return cacheStore.setXx(key, value);
    }

    public Boolean setEx(String key, Object value, long seconds) {
        if (seconds <= 0) {
            return false;
        }
        return cacheStore.setEx(key, value, Duration.ofSeconds(seconds));
    }

    public Object get(String key) {
        return cacheStore.get(key);
    }

    public Object getSet(String key, Object value) {
        return cacheStore.getSet(key, value);
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

    public Long incr(String key) {
        return cacheStore.incrementBy(key, 1);
    }

    public Long incrBy(String key, long delta) {
        return cacheStore.incrementBy(key, delta);
    }

    public Long decr(String key) {
        return cacheStore.incrementBy(key, -1);
    }

    public Long append(String key, String value) {
        return cacheStore.append(key, value);
    }

    public boolean delete(String key) {
        return cacheStore.delete(key);
    }

    public boolean del(String key) {
        return delete(key);
    }

    public boolean hasKey(String key) {
        return cacheStore.hasKey(key);
    }

    public boolean exists(String key) {
        return hasKey(key);
    }

    public boolean expire(String key, long ttl, TimeUnit unit) {
        if (ttl <= 0 || unit == null) {
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

    public Long hset(String key, String field, Object value) {
        return cacheStore.hset(key, field, value);
    }

    public Object hget(String key, String field) {
        return cacheStore.hget(key, field);
    }

    public java.util.List<Object> hmget(String key, java.util.List<String> fields) {
        return cacheStore.hmget(key, fields);
    }

    public java.util.List<Object> hmget(String key, String... fields) {
        if (fields == null || fields.length == 0) {
            return java.util.Collections.emptyList();
        }
        return cacheStore.hmget(key, java.util.Arrays.asList(fields));
    }

    public Long hincrBy(String key, String field, long delta) {
        return cacheStore.hincrBy(key, field, delta);
    }

    public Long lpush(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return cacheStore.lpush(key, java.util.Arrays.asList(values));
    }

    public Long rpush(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return cacheStore.rpush(key, java.util.Arrays.asList(values));
    }

    public Object lpop(String key) {
        return cacheStore.lpop(key);
    }

    public Object rpop(String key) {
        return cacheStore.rpop(key);
    }

    public Object brpop(String key, long timeout, TimeUnit unit) {
        return cacheStore.brpop(key, timeout, unit);
    }

    public Long sadd(String key, Object... members) {
        if (members == null || members.length == 0) {
            return 0L;
        }
        return cacheStore.sadd(key, java.util.Arrays.asList(members));
    }

    public Long srem(String key, Object... members) {
        if (members == null || members.length == 0) {
            return 0L;
        }
        return cacheStore.srem(key, java.util.Arrays.asList(members));
    }

    public Boolean sismember(String key, Object member) {
        return cacheStore.sismember(key, member);
    }

    public Long zadd(String key, double score, String member) {
        return cacheStore.zadd(key, score, member);
    }

    public Double zincrBy(String key, double delta, String member) {
        return cacheStore.zincrBy(key, delta, member);
    }

    public Long zrem(String key, String... members) {
        if (members == null || members.length == 0) {
            return 0L;
        }
        return cacheStore.zrem(key, java.util.Arrays.asList(members));
    }

    public boolean multiSet(java.util.Map<String, Object> values) {
        return cacheStore.multiSet(values);
    }

    public boolean multi(java.util.Map<String, Object> values) {
        return cacheStore.multiSet(values);
    }

    /**
     * Acquire a lock with auto-generated token.
     *
     * @param key lock key
     * @param ttl lock TTL
     * @return token when acquired, otherwise null
     */
    public String tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        return tryLock(key, token, ttl) ? token : null;
    }

    /**
     * Acquire a lock with given token.
     *
     * @param key   lock key
     * @param token lock token
     * @param ttl   lock TTL
     * @return true when acquired
     */
    public boolean tryLock(String key, String token, Duration ttl) {
        String lockKey = buildLockKey(key);
        Duration effectiveTtl = ttl == null ? DEFAULT_LOCK_TTL : ttl;
        return cacheStore.tryLock(lockKey, token, effectiveTtl);
    }

    /**
     * Release a lock with token.
     *
     * @param key   lock key
     * @param token lock token
     * @return true when released
     */
    public boolean releaseLock(String key, String token) {
        return cacheStore.releaseLock(buildLockKey(key), token);
    }

    /**
     * Execute action with lock protection. If not acquired, returns null.
     */
    public <T> T withLock(String key, Duration ttl, Supplier<T> action) {
        if (action == null) {
            return null;
        }
        String token = tryLock(key, ttl);
        if (token == null) {
            return null;
        }
        try {
            return action.get();
        } finally {
            releaseLock(key, token);
        }
    }

    /**
     * Execute runnable with lock protection. If not acquired, do nothing.
     */
    public boolean withLock(String key, Duration ttl, Runnable action) {
        if (action == null) {
            return false;
        }
        String token = tryLock(key, ttl);
        if (token == null) {
            return false;
        }
        try {
            action.run();
            return true;
        } finally {
            releaseLock(key, token);
        }
    }

    private String buildLockKey(String key) {
        if (key == null) {
            return null;
        }
        if (key.startsWith(LOCK_KEY_PREFIX)) {
            return key;
        }
        return LOCK_KEY_PREFIX + key;
    }
}
