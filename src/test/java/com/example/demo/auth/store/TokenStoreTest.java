package com.example.demo.auth.store;

import com.example.demo.auth.model.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TokenStoreTest {

    @Test
    void saveGetRevoke() {
        TokenStore store = new TokenStore(mockRedisTemplate());
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Alice");
        long expireAt = Instant.now().getEpochSecond() + 10;

        store.save("token", user, expireAt);
        TokenStore.TokenRecord record = store.get("token");
        assertEquals(user, record.getUser());

        store.revoke("token");
        assertNull(store.get("token"));
    }

    @Test
    void get_returnsNullWhenExpired() {
        TokenStore store = new TokenStore(mockRedisTemplate());
        AuthUser user = new AuthUser();
        user.setId(2L);
        user.setUserName("bob");
        user.setNickName("Bob");
        long expiredAt = Instant.now().getEpochSecond() - 1;

        store.save("expired", user, expiredAt);
        assertNull(store.get("expired"));
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
