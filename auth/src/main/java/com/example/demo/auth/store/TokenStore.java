package com.example.demo.auth.store;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.cache.CacheTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 令牌存储，基于缓存工具。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class TokenStore {

    private final CacheTool cacheTool;
    private final ObjectMapper objectMapper;
    private final AuthConstants systemConstants;

    /**
     * 构造函数，注入缓存工具。
     *
     * @param cacheTool 缓存工具
     */
    public TokenStore(CacheTool cacheTool, ObjectMapper objectMapper, AuthConstants systemConstants) {
        this.cacheTool = cacheTool;
        this.objectMapper = objectMapper;
        this.systemConstants = systemConstants;
    }

    /**
     * 保存令牌与用户信息，并设置过期时间。
     *
     * @param token           令牌字符串
     * @param user            认证用户摘要
     * @param expireAtSeconds 过期时间戳（秒）
     */
    public void save(String token, AuthUser user, long expireAtSeconds) {
        if (token == null || user == null) {
            return;
        }
        Long userId = user.getId();
        if (userId != null) {
            cacheTool.set(buildUserTokenKey(userId), token, Duration.ofSeconds(remainingSeconds(expireAtSeconds)));
            ensureVersionKey(userId, remainingSeconds(expireAtSeconds));
        }
        long ttlSeconds = expireAtSeconds - Instant.now().getEpochSecond();
        if (ttlSeconds <= 0) {
            return;
        }
        cacheTool.set(buildKey(token), new TokenRecord(user, expireAtSeconds), Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 根据令牌获取用户信息，若已过期则返回 null 并清理。
     *
     * @param token 令牌字符串
     * @return 令牌记录，未命中或过期返回 null
     */
    public TokenRecord get(String token) {
        if (token == null) {
            return null;
        }
        Object value = cacheTool.get(buildKey(token));
        TokenRecord record = convertRecord(value);
        if (record == null) {
            return null;
        }
        long now = Instant.now().getEpochSecond();
        if (record.getExpireAtSeconds() < now) {
            cacheTool.delete(buildKey(token));
            return null;
        }
        return record;
    }

    /**
     * 撤销令牌并删除存储。
     *
     * @param token 令牌字符串
     */
    public void revoke(String token) {
        if (token != null) {
            cacheTool.delete(buildKey(token));
        }
    }

    /**
     * 撤销用户的全部令牌，通过递增版本号使历史 token 失效。
     *
     * @param userId 用户 ID
     */
    public void revokeByUserId(Long userId, long ttlSeconds) {
        if (userId == null) {
            return;
        }
        String userKey = buildUserTokenKey(userId);
        String token = cacheTool.get(userKey, String.class);
        if (token != null) {
            cacheTool.delete(buildKey(token));
        }
        cacheTool.delete(userKey);
        bumpUserTokenVersion(userId, ttlSeconds);
    }

    /**
     * 构建缓存 Key。
     *
     * @param token 令牌字符串
     * @return 缓存 Key
     */
    private String buildKey(String token) {
        return systemConstants.getToken().getStoreKeyPrefix() + token;
    }

    private String buildUserTokenKey(Long userId) {
        return systemConstants.getToken().getStoreKeyPrefix() + "user:" + userId;
    }

    private String buildUserTokenVersionKey(Long userId) {
        return systemConstants.getToken().getStoreKeyPrefix() + "ver:" + userId;
    }

    private long remainingSeconds(long expireAtSeconds) {
        long ttlSeconds = expireAtSeconds - Instant.now().getEpochSecond();
        return Math.max(ttlSeconds, 1);
    }

    public long getOrInitVersion(Long userId, long ttlSeconds) {
        long current = getUserTokenVersion(userId);
        if (current > 0) {
            return current;
        }
        long initial = 1L;
        setUserTokenVersion(userId, initial, ttlSeconds);
        return initial;
    }

    public long getUserTokenVersion(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Object value = cacheTool.get(buildUserTokenVersionKey(userId));
        Long parsed = parseLong(value);
        return parsed == null ? 0L : parsed;
    }

    public void setUserTokenVersion(Long userId, long version, long ttlSeconds) {
        if (userId == null) {
            return;
        }
        cacheTool.set(buildUserTokenVersionKey(userId), version, Duration.ofSeconds(versionTtlSeconds(ttlSeconds)));
    }

    public long bumpUserTokenVersion(Long userId, long ttlSeconds) {
        if (userId == null) {
            return 0L;
        }
        String key = buildUserTokenVersionKey(userId);
        Long next = cacheTool.increment(key);
        long value = next == null ? 1L : next.longValue();
        cacheTool.expire(key, Duration.ofSeconds(versionTtlSeconds(ttlSeconds)));
        return value;
    }

    private void ensureVersionKey(Long userId, long ttlSeconds) {
        if (userId == null) {
            return;
        }
        String key = buildUserTokenVersionKey(userId);
        if (!cacheTool.hasKey(key)) {
            setUserTokenVersion(userId, 1L, ttlSeconds);
        }
    }

    private long versionTtlSeconds(long ttlSeconds) {
        long base = Math.max(1L, ttlSeconds);
        long doubled = base * 2;
        return Math.max(doubled, base + 60);
    }

    private Long parseLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private TokenRecord convertRecord(Object value) {
        if (value instanceof TokenRecord) {
            return (TokenRecord) value;
        }
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(value, TokenRecord.class);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * 令牌记录载体，包含用户摘要与过期时间。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenRecord {
        private AuthUser user;
        private long expireAtSeconds;
    }
}
