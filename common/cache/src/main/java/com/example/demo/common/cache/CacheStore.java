package com.example.demo.common.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    Boolean set(String key, Object value);

    Boolean setNx(String key, Object value);

    Boolean setXx(String key, Object value);

    Boolean setEx(String key, Object value, Duration ttl);

    Object getSet(String key, Object value);

    Long incrementBy(String key, long delta);

    Long append(String key, String value);

    Long hset(String key, String field, Object value);

    Object hget(String key, String field);

    List<Object> hmget(String key, List<String> fields);

    Long hincrBy(String key, String field, long delta);

    Long lpush(String key, List<Object> values);

    Long rpush(String key, List<Object> values);

    Object lpop(String key);

    Object rpop(String key);

    Object brpop(String key, long timeout, TimeUnit unit);

    Long sadd(String key, Collection<Object> members);

    Long srem(String key, Collection<Object> members);

    Boolean sismember(String key, Object member);

    Long zadd(String key, double score, String member);

    Double zincrBy(String key, double delta, String member);

    Long zrem(String key, Collection<String> members);

    boolean multiSet(Map<String, Object> values);

    /**
     * Try to acquire a lock for the given key with a token and TTL.
     *
     * @param key   lock key
     * @param token lock token
     * @param ttl   lock TTL
     * @return true when acquired
     */
    boolean tryLock(String key, String token, Duration ttl);

    /**
     * Release the lock when the token matches.
     *
     * @param key   lock key
     * @param token lock token
     * @return true when released
     */
    boolean releaseLock(String key, String token);
}
