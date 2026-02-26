package com.example.demo.auth.store;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.cache.CacheTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 令牌存储，基于缓存工具。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class TokenStore {

    private static final String VERSION_FIELD = "ver";
    private static final String TOKEN_FIELD_PREFIX = "token:";
    private static final String TOKEN_INDEX_PREFIX = "idx:";

    private final CacheTool cacheTool;
    private final ObjectMapper objectMapper;
    private final AuthConstants systemConstants;
    private final AuthProperties authProperties;

    /**
     * 构造函数，注入缓存工具。
     *
     * @param cacheTool 缓存工具
     */
    public TokenStore(CacheTool cacheTool,
                      ObjectMapper objectMapper,
                      AuthConstants systemConstants,
                      AuthProperties authProperties) {
        this.cacheTool = cacheTool;
        this.objectMapper = objectMapper;
        this.systemConstants = systemConstants;
        this.authProperties = authProperties;
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
        long ttlSeconds = expireAtSeconds - Instant.now().getEpochSecond();
        if (ttlSeconds <= 0) {
            return;
        }
        if (userId != null) {
            cacheTool.hset(buildUserTokenKey(userId), buildTokenField(token), new TokenRecord(user, expireAtSeconds));
            ensureBucketTtl(userId, bucketTtlSeconds(ttlSeconds));
            cacheTool.set(buildTokenIndexKey(token), userId, Duration.ofSeconds(ttlSeconds));
        }
    }

    /**
     * 单次读取令牌记录与版本号。
     *
     * @param userId 用户 ID
     * @param token  令牌字符串
     * @return 令牌快照，未命中返回 null
     */
    public TokenSnapshot getSnapshot(Long userId, String token) {
        if (userId == null || token == null) {
            return null;
        }
        String key = buildUserTokenKey(userId);
        List<Object> values = cacheTool.hmget(key, VERSION_FIELD, buildTokenField(token));
        if (values == null || values.size() < 2) {
            return null;
        }
        Long version = parseLong(values.get(0));
        TokenRecord record = convertRecord(values.get(1));
        if (record == null) {
            return new TokenSnapshot(null, version == null ? 0L : version);
        }
        long now = Instant.now().getEpochSecond();
        if (record.getExpireAtSeconds() < now) {
            cacheTool.hdel(key, buildTokenField(token));
            cacheTool.delete(buildTokenIndexKey(token));
            return new TokenSnapshot(null, version == null ? 0L : version);
        }
        return new TokenSnapshot(record, version == null ? 0L : version);
    }

    /**
     * 撤销令牌并删除存储。
     *
     * @param token 令牌字符串
     */
    public void revoke(String token) {
        if (token != null) {
            Long userId = resolveUserIdFromIndex(token);
            if (userId != null) {
                revoke(userId, token);
            } else {
                cacheTool.delete(buildTokenIndexKey(token));
            }
        }
    }

    /**
     * 按用户撤销令牌并删除存储。
     *
     * @param userId 用户 ID
     * @param token  令牌字符串
     */
    public void revoke(Long userId, String token) {
        if (userId == null || token == null) {
            return;
        }
        cacheTool.hdel(buildUserTokenKey(userId), buildTokenField(token));
        cacheTool.delete(buildTokenIndexKey(token));
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
        bumpUserTokenVersion(userId, ttlSeconds);
    }

    private String buildUserTokenKey(Long userId) {
        return systemConstants.getToken().getStoreKeyPrefix() + "user:" + userId;
    }

    private String buildTokenField(String token) {
        return TOKEN_FIELD_PREFIX + token;
    }

    private String buildTokenIndexKey(String token) {
        return systemConstants.getToken().getStoreKeyPrefix() + TOKEN_INDEX_PREFIX + token;
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
        Object value = cacheTool.hget(buildUserTokenKey(userId), VERSION_FIELD);
        Long parsed = parseLong(value);
        return parsed == null ? 0L : parsed;
    }

    public void setUserTokenVersion(Long userId, long version, long ttlSeconds) {
        if (userId == null) {
            return;
        }
        cacheTool.hset(buildUserTokenKey(userId), VERSION_FIELD, version);
        ensureBucketTtl(userId, versionTtlSeconds(ttlSeconds));
    }

    public long bumpUserTokenVersion(Long userId, long ttlSeconds) {
        if (userId == null) {
            return 0L;
        }
        String key = buildUserTokenKey(userId);
        Long next = cacheTool.hincrBy(key, VERSION_FIELD, 1L);
        long value = next == null ? 1L : next;
        ensureBucketTtl(userId, versionTtlSeconds(ttlSeconds));
        return value;
    }

    private long versionTtlSeconds(long ttlSeconds) {
        long configured = authProperties.getJwt().getTokenVersionTtlSeconds();
        if (configured > 0) {
            return configured;
        }
        long base = Math.max(1L, ttlSeconds);
        long doubled = base * 2;
        return Math.max(doubled, base + 600);
    }

    private long bucketTtlSeconds(long tokenTtlSeconds) {
        return Math.max(tokenTtlSeconds, versionTtlSeconds(tokenTtlSeconds));
    }

    private void ensureBucketTtl(Long userId, long ttlSeconds) {
        if (userId == null || ttlSeconds <= 0) {
            return;
        }
        String key = buildUserTokenKey(userId);
        long current = cacheTool.getExpire(key, TimeUnit.SECONDS);
        if (current < ttlSeconds) {
            cacheTool.expire(key, Duration.ofSeconds(ttlSeconds));
        }
    }

    private Long resolveUserIdFromIndex(String token) {
        Object value = cacheTool.get(buildTokenIndexKey(token));
        Long parsed = parseLong(value);
        if (parsed != null) {
            return parsed;
        }
        return null;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenSnapshot {
        private TokenRecord record;
        private long version;
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
