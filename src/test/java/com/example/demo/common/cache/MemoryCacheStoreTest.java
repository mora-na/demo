package com.example.demo.common.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class MemoryCacheStoreTest {

    @Test
    void setGetExpires() throws Exception {
        CacheProperties.Memory props = new CacheProperties.Memory();
        props.setCleanupIntervalSeconds(0);
        props.setMaximumWeightMb(0);
        MemoryCacheStore store = new MemoryCacheStore(props);

        store.set("k1", "v1", Duration.ofMillis(30));
        assertEquals("v1", store.get("k1"));
        Thread.sleep(50);
        assertNull(store.get("k1"));
    }

    @Test
    void setIfAbsentAndIncrement() {
        CacheProperties.Memory props = new CacheProperties.Memory();
        props.setCleanupIntervalSeconds(0);
        props.setMaximumWeightMb(0);
        MemoryCacheStore store = new MemoryCacheStore(props);

        assertTrue(store.setIfAbsent("k2", "v2", Duration.ofSeconds(5)));
        assertFalse(store.setIfAbsent("k2", "v3", Duration.ofSeconds(5)));
        assertEquals(1L, store.increment("counter"));
        assertEquals(2L, store.increment("counter"));
    }

    @Test
    void maximumWeightEvictsEntries() {
        CacheProperties.Memory props = new CacheProperties.Memory();
        props.setCleanupIntervalSeconds(0);
        props.setMaximumWeightMb(1);
        MemoryCacheStore store = new MemoryCacheStore(props);

        store.set("a", new byte[700 * 1024], Duration.ofSeconds(10));
        store.set("b", new byte[700 * 1024], Duration.ofSeconds(10));
        boolean bothPresent = store.hasKey("a") && store.hasKey("b");
        assertFalse(bothPresent, "weight limit should evict at least one entry");
    }
}
