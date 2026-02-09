package com.example.demo.auth.store;

import com.example.demo.auth.model.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 令牌存储，基于 Redis 保存令牌与用户信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class TokenStore {

    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数，注入 Redis 模板。
     *
     * @param redisTemplate Redis 模板
     */
    public TokenStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
        long ttlSeconds = expireAtSeconds - Instant.now().getEpochSecond();
        if (ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue()
                .set(buildKey(token), new TokenRecord(user, expireAtSeconds), Duration.ofSeconds(ttlSeconds));
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
        Object value = redisTemplate.opsForValue().get(buildKey(token));
        if (!(value instanceof TokenRecord)) {
            return null;
        }
        TokenRecord record = (TokenRecord) value;
        if (record == null) {
            return null;
        }
        long now = Instant.now().getEpochSecond();
        if (record.getExpireAtSeconds() < now) {
            redisTemplate.delete(buildKey(token));
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
            redisTemplate.delete(buildKey(token));
        }
    }

    /**
     * 构建 Redis Key。
     *
     * @param token 令牌字符串
     * @return Redis Key
     */
    private String buildKey(String token) {
        return TOKEN_KEY_PREFIX + token;
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
