package com.example.demo.common.cache;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存存储实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class RedisCacheStore implements CacheStore {

    private static final String RELEASE_LOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT =
            new DefaultRedisScript<>(RELEASE_LOCK_LUA, Long.class);

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
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            set(key, value);
            return;
        }
        if (value instanceof String) {
            stringRedisTemplate.opsForValue().set(key, (String) value, ttl);
            return;
        }
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            return null;
        }
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return value;
            }
        } catch (SerializationException ex) {
            // Fall back to string storage for legacy/simple string values.
        }
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        if (key == null) {
            return false;
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return setNx(key, value);
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
        return stringRedisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        if (key == null) {
            return false;
        }
        return stringRedisTemplate.hasKey(key);
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
        return stringRedisTemplate.getExpire(key, unit);
    }

    @Override
    public Boolean set(String key, Object value) {
        if (key == null) {
            return false;
        }
        if (value instanceof String) {
            stringRedisTemplate.opsForValue().set(key, (String) value);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
        return true;
    }

    @Override
    public Boolean setNx(String key, Object value) {
        if (key == null) {
            return false;
        }
        if (value instanceof String) {
            return stringRedisTemplate.opsForValue().setIfAbsent(key, (String) value);
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    @Override
    public Boolean setXx(String key, Object value) {
        if (key == null) {
            return false;
        }
        if (value instanceof String) {
            return stringRedisTemplate.opsForValue().setIfPresent(key, (String) value);
        }
        return redisTemplate.opsForValue().setIfPresent(key, value);
    }

    @Override
    public Boolean setEx(String key, Object value, Duration ttl) {
        if (key == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return false;
        }
        if (value instanceof String) {
            stringRedisTemplate.opsForValue().set(key, (String) value, ttl);
        } else {
            redisTemplate.opsForValue().set(key, value, ttl);
        }
        return true;
    }

    @Override
    public Object getSet(String key, Object value) {
        if (key == null) {
            return null;
        }
        if (value instanceof String) {
            return stringRedisTemplate.opsForValue().getAndSet(key, (String) value);
        }
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    @Override
    public Long incrementBy(String key, long delta) {
        if (key == null) {
            return null;
        }
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long append(String key, String value) {
        if (key == null) {
            return null;
        }
        Integer length = stringRedisTemplate.opsForValue().append(key, value == null ? "" : value);
        return length == null ? null : length.longValue();
    }

    @Override
    public Long hset(String key, String field, Object value) {
        if (key == null || field == null) {
            return null;
        }
        Boolean created = redisTemplate.opsForHash().putIfAbsent(key, field, value);
        return created == null ? null : (created ? 1L : 0L);
    }

    @Override
    public Object hget(String key, String field) {
        if (key == null || field == null) {
            return null;
        }
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public List<Object> hmget(String key, List<String> fields) {
        if (key == null || fields == null) {
            return null;
        }
        if (fields.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Object> castFields = new java.util.ArrayList<>(fields.size());
        for (String field : fields) {
            castFields.add(field);
        }
        return redisTemplate.opsForHash().multiGet(key, castFields);
    }

    @Override
    public Long hincrBy(String key, String field, long delta) {
        if (key == null || field == null) {
            return null;
        }
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    @Override
    public Long hdel(String key, String... fields) {
        if (key == null || fields == null || fields.length == 0) {
            return 0L;
        }
        Object[] castFields = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            castFields[i] = fields[i];
        }
        Long removed = redisTemplate.opsForHash().delete(key, castFields);
        return removed == null ? 0L : removed;
    }

    @Override
    public Long lpush(String key, List<Object> values) {
        if (key == null || values == null || values.isEmpty()) {
            return 0L;
        }
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    @Override
    public Long rpush(String key, List<Object> values) {
        if (key == null || values == null || values.isEmpty()) {
            return 0L;
        }
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public Object lpop(String key) {
        if (key == null) {
            return null;
        }
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public Object rpop(String key) {
        if (key == null) {
            return null;
        }
        return redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public Object brpop(String key, long timeout, TimeUnit unit) {
        if (key == null || unit == null) {
            return null;
        }
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    @Override
    public Long sadd(String key, Collection<Object> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        return redisTemplate.opsForSet().add(key, members.toArray());
    }

    @Override
    public Long srem(String key, Collection<Object> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        return redisTemplate.opsForSet().remove(key, members.toArray());
    }

    @Override
    public Boolean sismember(String key, Object member) {
        if (key == null) {
            return false;
        }
        return redisTemplate.opsForSet().isMember(key, member);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        if (key == null || member == null) {
            return null;
        }
        Boolean created = redisTemplate.opsForZSet().add(key, member, score);
        if (created == null) {
            return null;
        }
        return created ? 1L : 0L;
    }

    @Override
    public Double zincrBy(String key, double delta, String member) {
        if (key == null || member == null) {
            return null;
        }
        return redisTemplate.opsForZSet().incrementScore(key, member, delta);
    }

    @Override
    public Long zrem(String key, Collection<String> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        return redisTemplate.opsForZSet().remove(key, members.toArray());
    }

    @Override
    public boolean multiSet(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        List<Object> result = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    if (entry.getKey() == null) {
                        continue;
                    }
                    operations.opsForValue().set(entry.getKey(), entry.getValue());
                }
                return operations.exec();
            }
        });
        return result != null;
    }

    @Override
    public List<Object> multiGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return redisTemplate.opsForValue().multiGet(keys);
    }

    @Override
    public boolean tryLock(String key, String token, Duration ttl) {
        if (key == null || token == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return false;
        }
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public boolean releaseLock(String key, String token) {
        if (key == null || token == null) {
            return false;
        }
        Long released = stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT,
                Collections.singletonList(key),
                token);
        return released != null && released > 0;
    }
}
