package com.example.demo.common.cache;

import com.example.demo.common.cache.mapper.CacheMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DbCacheStoreTest {

    @Test
    void setIfAbsentHonorsExpiry() throws Exception {
        Map<String, CacheEntry> store = new ConcurrentHashMap<>();
        CacheMapper mapper = mockCacheMapper(store);
        CacheProperties.Db dbProps = new CacheProperties.Db();
        dbProps.setCleanupIntervalSeconds(0);
        dbProps.setMaximumRows(0);
        DbCacheStore cacheStore = new DbCacheStore(mapper, new CacheSerializer(new ObjectMapper()), dbProps);

        assertTrue(cacheStore.setIfAbsent("k1", "v1", Duration.ofMillis(20)));
        assertFalse(cacheStore.setIfAbsent("k1", "v2", Duration.ofMillis(20)));

        Thread.sleep(30);
        assertTrue(cacheStore.setIfAbsent("k1", "v3", Duration.ofMillis(20)));
    }

    @Test
    void incrementAndGet() {
        Map<String, CacheEntry> store = new ConcurrentHashMap<>();
        CacheMapper mapper = mockCacheMapper(store);
        CacheProperties.Db dbProps = new CacheProperties.Db();
        dbProps.setCleanupIntervalSeconds(0);
        dbProps.setMaximumRows(0);
        DbCacheStore cacheStore = new DbCacheStore(mapper, new CacheSerializer(new ObjectMapper()), dbProps);

        assertEquals(1L, cacheStore.increment("counter"));
        assertEquals(2L, cacheStore.increment("counter"));
        assertEquals(2L, cacheStore.get("counter"));
    }

    @Test
    void enforceMaxRowsEvictsOldestExpire() {
        Map<String, CacheEntry> store = new ConcurrentHashMap<>();
        CacheMapper mapper = mockCacheMapper(store);
        CacheProperties.Db dbProps = new CacheProperties.Db();
        dbProps.setCleanupIntervalSeconds(0);
        dbProps.setMaximumRows(1);
        DbCacheStore cacheStore = new DbCacheStore(mapper, new CacheSerializer(new ObjectMapper()), dbProps);

        cacheStore.set("k1", "v1", Duration.ofMillis(50));
        cacheStore.set("k2", "v2", Duration.ofSeconds(5));

        assertFalse(cacheStore.hasKey("k1"));
        assertTrue(cacheStore.hasKey("k2"));
    }

    private CacheMapper mockCacheMapper(Map<String, CacheEntry> store) {
        CacheMapper mapper = mock(CacheMapper.class);

        when(mapper.selectById(anyString())).thenAnswer(inv -> store.get(inv.getArgument(0)));
        when(mapper.selectForUpdate(anyString())).thenAnswer(inv -> store.get(inv.getArgument(0)));
        when(mapper.selectCount(any())).thenAnswer(inv -> (long) store.size());
        when(mapper.selectKeysForEviction(anyInt())).thenAnswer(inv -> {
            int limit = inv.getArgument(0);
            return store.values().stream()
                    .sorted(Comparator
                            .comparing((CacheEntry e) -> e.getExpireAt() == null)
                            .thenComparing(e -> e.getExpireAt() == null ? Long.MAX_VALUE : e.getExpireAt())
                            .thenComparing(CacheEntry::getCacheKey))
                    .limit(limit)
                    .map(CacheEntry::getCacheKey)
                    .collect(Collectors.toList());
        });

        doAnswer(inv -> {
            CacheEntry entry = inv.getArgument(0);
            if (entry == null || entry.getCacheKey() == null) {
                return 0;
            }
            if (store.containsKey(entry.getCacheKey())) {
                throw new DuplicateKeyException("duplicate key");
            }
            store.put(entry.getCacheKey(), cloneEntry(entry));
            return 1;
        }).when(mapper).insert(any(CacheEntry.class));

        doAnswer(inv -> {
            CacheEntry entry = inv.getArgument(0);
            if (entry == null || entry.getCacheKey() == null) {
                return 0;
            }
            store.put(entry.getCacheKey(), cloneEntry(entry));
            return 1;
        }).when(mapper).updateById(any(CacheEntry.class));

        doAnswer(inv -> store.remove(inv.getArgument(0)) == null ? 0 : 1)
                .when(mapper).deleteById(anyString());

        doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Collection<String> keys = (Collection<String>) inv.getArgument(0);
            int count = 0;
            if (keys != null) {
                for (String key : keys) {
                    if (store.remove(key) != null) {
                        count++;
                    }
                }
            }
            return count;
        }).when(mapper).deleteBatchIds(any());

        doAnswer(inv -> {
            long now = inv.getArgument(0);
            List<String> expired = new ArrayList<>();
            for (CacheEntry entry : store.values()) {
                if (entry.getExpireAt() != null && entry.getExpireAt() < now) {
                    expired.add(entry.getCacheKey());
                }
            }
            for (String key : expired) {
                store.remove(key);
            }
            return expired.size();
        }).when(mapper).deleteExpired(anyLong());

        return mapper;
    }

    private CacheEntry cloneEntry(CacheEntry entry) {
        CacheEntry cloned = new CacheEntry();
        cloned.setCacheKey(entry.getCacheKey());
        cloned.setCacheValue(entry.getCacheValue());
        cloned.setValueClass(entry.getValueClass());
        cloned.setExpireAt(entry.getExpireAt());
        return cloned;
    }
}
