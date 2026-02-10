package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.store.TokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Test
    void issueAndVerifyToken() {
        AuthProperties properties = new AuthProperties();
        properties.getJwt().setSecret("test-secret");
        properties.getJwt().setTtlSeconds(60);
        TokenStore store = new TokenStore(mockRedisTemplate());
        TokenService service = new TokenService(properties, store);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Alice");
        String token = service.issueToken(user).getToken();
        assertNotNull(token);

        AuthUser verified = service.verifyToken(token);
        assertNotNull(verified);
        assertEquals(user.getUserName(), verified.getUserName());
    }

    @Test
    void verifyToken_returnsNullWhenExpired() {
        AuthProperties properties = new AuthProperties();
        properties.getJwt().setSecret("test-secret");
        properties.getJwt().setTtlSeconds(-1);
        TokenStore store = new TokenStore(mockRedisTemplate());
        TokenService service = new TokenService(properties, store);

        AuthUser user = new AuthUser();
        user.setId(2L);
        user.setUserName("bob");
        user.setNickName("Bob");
        String token = service.issueToken(user).getToken();
        assertNull(service.verifyToken(token));
        assertNull(store.get(token));
    }

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> mockRedisTemplate() {
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        Map<String, Object> store = new ConcurrentHashMap<>();
        Map<String, Long> expiry = new ConcurrentHashMap<>();
        when(template.opsForValue()).thenReturn(valueOps);
        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            Duration ttl = inv.getArgument(2);
            expiry.put(inv.getArgument(0), System.currentTimeMillis() + ttl.toMillis());
            return null;
        }).when(valueOps).set(anyString(), any(), any(Duration.class));
        when(valueOps.get(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            Long expireAt = expiry.get(key);
            if (expireAt != null && System.currentTimeMillis() > expireAt) {
                store.remove(key);
                expiry.remove(key);
                return null;
            }
            return store.get(key);
        });
        when(template.delete(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            expiry.remove(key);
            return store.remove(key) != null;
        });
        return template;
    }
}
