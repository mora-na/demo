package com.example.demo.auth.store;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CaptchaStoreTest {

    @Test
    void saveAndVerify_successThenRemoved() {
        CaptchaStore store = new CaptchaStore(mockStringRedisTemplate());
        long expireAt = Instant.now().getEpochSecond() + 5;
        store.save("id-1", "abcd", expireAt);

        assertTrue(store.verifyAndRemove("id-1", "abcd"));
        assertFalse(store.verifyAndRemove("id-1", "abcd"));
    }

    @Test
    void verify_failsWhenExpiredOrWrongCode() throws Exception {
        CaptchaStore store = new CaptchaStore(mockStringRedisTemplate());
        long expiredAt = Instant.now().getEpochSecond() - 1;
        store.save("id-2", "xyz", expiredAt);

        Thread.sleep(1100);
        assertFalse(store.verifyAndRemove("id-2", "xyz"));

        long expireAt = Instant.now().getEpochSecond() + 5;
        store.save("id-3", "good", expireAt);
        assertFalse(store.verifyAndRemove("id-3", "bad"));
    }

    private StringRedisTemplate mockStringRedisTemplate() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        Map<String, String> store = new ConcurrentHashMap<>();
        Map<String, Long> expiry = new ConcurrentHashMap<>();
        when(template.opsForValue()).thenReturn(valueOps);
        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            Duration ttl = inv.getArgument(2);
            expiry.put(inv.getArgument(0), System.currentTimeMillis() + ttl.toMillis());
            return null;
        }).when(valueOps).set(anyString(), anyString(), any(Duration.class));
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
