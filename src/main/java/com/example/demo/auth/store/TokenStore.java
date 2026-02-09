package com.example.demo.auth.store;

import com.example.demo.auth.model.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TokenStore {

    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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

    public void revoke(String token) {
        if (token != null) {
            redisTemplate.delete(buildKey(token));
        }
    }

    private String buildKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenRecord {
        private AuthUser user;
        private long expireAtSeconds;
    }
}
