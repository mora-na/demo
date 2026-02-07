package com.example.demo.auth.store;

import com.example.demo.auth.model.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();

    public void save(String token, AuthUser user, long expireAtSeconds) {
        tokens.put(token, new TokenRecord(user, expireAtSeconds));
    }

    public TokenRecord get(String token) {
        if (token == null) {
            return null;
        }
        TokenRecord record = tokens.get(token);
        if (record == null) {
            return null;
        }
        long now = Instant.now().getEpochSecond();
        if (record.getExpireAtSeconds() < now) {
            tokens.remove(token);
            return null;
        }
        return record;
    }

    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    @Data
    @AllArgsConstructor
    public static class TokenRecord {
        private AuthUser user;
        private long expireAtSeconds;
    }
}
