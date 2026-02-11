package com.example.demo.common.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RedisCacheStoreTest {

    @Test
    void getFallsBackToStringTemplate() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> stringOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringOps);
        when(valueOps.get("k1")).thenReturn(null);
        when(stringOps.get("k1")).thenReturn("v1");

        RedisCacheStore store = new RedisCacheStore(redisTemplate, stringRedisTemplate);
        assertEquals("v1", store.get("k1"));
        when(valueOps.get("k2")).thenReturn(123);
        assertEquals(123, store.get("k2"));
        when(valueOps.get("k3")).thenReturn(null);
        when(stringOps.get("k3")).thenReturn(null);
        assertNull(store.get("k3"));
    }

    @Test
    void setIfAbsentUsesStringTemplateForStrings() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> stringOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringOps);
        when(stringOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        RedisCacheStore store = new RedisCacheStore(redisTemplate, stringRedisTemplate);
        store.setIfAbsent("k1", "v1", Duration.ofSeconds(1));
        verify(stringOps, times(1)).setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(valueOps, never()).setIfAbsent(anyString(), any(), any(Duration.class));
    }
}
