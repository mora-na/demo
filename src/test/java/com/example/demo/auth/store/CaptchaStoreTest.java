package com.example.demo.auth.store;

import com.example.demo.common.cache.CacheProperties;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.cache.MemoryCacheStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaStoreTest {

    @Test
    void saveAndVerify_successThenRemoved() {
        CacheProperties.Memory memory = new CacheProperties.Memory();
        memory.setCleanupIntervalSeconds(0);
        memory.setMaximumWeightMb(0);
        CaptchaStore store = new CaptchaStore(new CacheTool(new MemoryCacheStore(memory)));
        long expireAt = Instant.now().getEpochSecond() + 5;
        store.save("id-1", "abcd", expireAt);

        assertTrue(store.verifyAndRemove("id-1", "abcd"));
        assertFalse(store.verifyAndRemove("id-1", "abcd"));
    }

    @Test
    void verify_failsWhenExpiredOrWrongCode() throws Exception {
        CacheProperties.Memory memory = new CacheProperties.Memory();
        memory.setCleanupIntervalSeconds(0);
        memory.setMaximumWeightMb(0);
        CaptchaStore store = new CaptchaStore(new CacheTool(new MemoryCacheStore(memory)));
        long expiredAt = Instant.now().getEpochSecond() - 1;
        store.save("id-2", "xyz", expiredAt);

        Thread.sleep(1100);
        assertFalse(store.verifyAndRemove("id-2", "xyz"));

        long expireAt = Instant.now().getEpochSecond() + 5;
        store.save("id-3", "good", expireAt);
        assertFalse(store.verifyAndRemove("id-3", "bad"));
    }
}
