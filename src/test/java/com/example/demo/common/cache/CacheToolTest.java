package com.example.demo.common.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CacheToolTest {

    @Test
    void basicOperations() throws Exception {
        CacheProperties.Memory props = new CacheProperties.Memory();
        props.setCleanupIntervalSeconds(0);
        props.setMaximumWeightMb(0);
        CacheTool tool = new CacheTool(new MemoryCacheStore(props));

        tool.set("k1", "value", Duration.ofSeconds(2));
        assertEquals("value", tool.get("k1", String.class));
        assertNull(tool.get("k1", Integer.class));

        assertTrue(tool.setIfAbsent("k2", "v2", Duration.ofSeconds(5)));
        assertFalse(tool.setIfAbsent("k2", "v3", Duration.ofSeconds(5)));

        assertEquals(1L, tool.increment("counter"));
        assertEquals(2L, tool.increment("counter"));

        tool.expire("k1", 1, TimeUnit.SECONDS);
        Thread.sleep(70);
        assertNotEquals(-2, tool.getExpire("k1", TimeUnit.MILLISECONDS));
    }
}
