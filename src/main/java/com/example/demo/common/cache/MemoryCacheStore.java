package com.example.demo.common.cache;

import com.example.demo.common.config.CommonConstants;
import com.example.demo.notice.mapper.NoticeRecipientMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存缓存存储实现，支持 TTL。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class MemoryCacheStore implements CacheStore, AutoCloseable {

    private static final long DEFAULT_MAXIMUM_SIZE = 10_000;
    private static final long DEFAULT_MAXIMUM_WEIGHT_BYTES = 64L * 1024 * 1024;
    private static final int MIN_WEIGHT = 1;
    private final Cache<String, CacheItem> cache;
    private final ScheduledExecutorService cleanupExecutor;
    private final long maximumWeightBytes;

    public MemoryCacheStore(CacheProperties.Memory memoryProperties, CommonConstants systemConstants) {
        long maxSize = memoryProperties == null ? DEFAULT_MAXIMUM_SIZE : memoryProperties.getMaximumSize();
        if (maxSize <= 0) {
            maxSize = DEFAULT_MAXIMUM_SIZE;
        }
        long maxWeight = resolveMaxWeightBytes(memoryProperties);
        this.maximumWeightBytes = maxWeight > 0 ? maxWeight : 0;
        Caffeine<String, CacheItem> builder = Caffeine.newBuilder()
                .expireAfter(new CacheItemExpiry());
        if (this.maximumWeightBytes > 0) {
            builder.maximumWeight(this.maximumWeightBytes)
                    .weigher(this::estimateWeight);
        } else {
            builder.maximumSize(maxSize);
        }
        this.cache = builder.build();
        long cleanupInterval = memoryProperties == null ? 0 : memoryProperties.getCleanupIntervalSeconds();
        if (cleanupInterval > 0) {
            String prefix = resolveCleanupThreadPrefix(systemConstants);
            this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(prefix));
            this.cleanupExecutor.scheduleAtFixedRate(cache::cleanUp, cleanupInterval, cleanupInterval, TimeUnit.SECONDS);
        } else {
            this.cleanupExecutor = null;
        }
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        if (key == null) {
            return;
        }
        if (maximumWeightBytes > 0 && estimateWeightBytes(key, value) > maximumWeightBytes) {
            cache.invalidate(key);
            return;
        }
        cache.put(key, new CacheItem(value, computeExpireAtNanos(ttl)));
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            return null;
        }
        CacheItem item = cache.getIfPresent(key);
        if (item == null) {
            return null;
        }
        return item.value;
    }

    @Override
    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        if (key == null) {
            return false;
        }
        if (maximumWeightBytes > 0 && estimateWeightBytes(key, value) > maximumWeightBytes) {
            return false;
        }
        Long expireAt = computeExpireAtNanos(ttl);
        AtomicBoolean created = new AtomicBoolean(false);
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                created.set(true);
                return new CacheItem(value, expireAt);
            }
            return existing;
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return created.get();
    }

    @Override
    public Long increment(String key) {
        if (key == null) {
            return null;
        }
        final long[] next = new long[1];
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                next[0] = 1L;
                return new CacheItem(1L, null);
            }
            long current = parseLong(existing.value);
            next[0] = current + 1;
            return new CacheItem(next[0], existing.expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return next[0];
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return cache.asMap().remove(key) != null;
    }

    @Override
    public boolean hasKey(String key) {
        if (key == null) {
            return false;
        }
        return cache.getIfPresent(key) != null;
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        if (key == null) {
            return false;
        }
        Long expireAt = computeExpireAtNanos(ttl);
        if (expireAt == null) {
            return false;
        }
        return cache.asMap().computeIfPresent(key, (k, existing) -> {
            if (existing.isExpired()) {
                return null;
            }
            return new CacheItem(existing.value, expireAt);
        }) != null;
    }

    @Override
    public long getExpire(String key, TimeUnit unit) {
        if (key == null) {
            return -2;
        }
        CacheItem item = cache.getIfPresent(key);
        if (item == null) {
            return -2;
        }
        if (item.expireAt == null || item.expireAt == Long.MAX_VALUE) {
            return -1;
        }
        long remainingNanos = item.expireAt - System.nanoTime();
        if (remainingNanos <= 0) {
            cache.invalidate(key);
            return -2;
        }
        return unit.convert(remainingNanos, TimeUnit.NANOSECONDS);
    }

    private Long computeExpireAtNanos(Duration ttl) {
        if (ttl == null) {
            return null;
        }
        long nanos = ttl.toNanos();
        if (nanos <= 0) {
            return null;
        }
        long now = System.nanoTime();
        try {
            return Math.addExact(now, nanos);
        } catch (ArithmeticException ex) {
            return Long.MAX_VALUE;
        }
    }

    private long parseLong(Object value) {
        return NoticeRecipientMapper.toLong(value);
    }

    private int estimateWeight(String key, CacheItem item) {
        long weight = estimateWeightBytes(key, item == null ? null : item.value);
        if (weight <= 0) {
            return MIN_WEIGHT;
        }
        if (weight > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) weight;
    }

    private long estimateWeightBytes(String key, Object value) {
        long weight = 0;
        if (key != null) {
            weight += (long) key.length() * 2;
        }
        if (value != null) {
            weight += estimateValueWeight(value);
        }
        return weight;
    }

    private long estimateValueWeight(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof String) {
            return (long) ((String) value).length() * 2;
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum) {
            return 32;
        }
        if (value instanceof java.util.Collection) {
            return 64L * ((java.util.Collection<?>) value).size() + 32;
        }
        if (value instanceof java.util.Map) {
            return 64L * ((java.util.Map<?, ?>) value).size() + 64;
        }
        if (value.getClass().isArray()) {
            return 64L * java.lang.reflect.Array.getLength(value) + 32;
        }
        return 256;
    }

    private long resolveMaxWeightBytes(CacheProperties.Memory memoryProperties) {
        if (memoryProperties == null) {
            return DEFAULT_MAXIMUM_WEIGHT_BYTES;
        }
        long mb = memoryProperties.getMaximumWeightMb();
        if (mb <= 0) {
            return 0;
        }
        try {
            return Math.multiplyExact(mb, 1024L * 1024L);
        } catch (ArithmeticException ex) {
            return Long.MAX_VALUE;
        }
    }

    private String resolveCleanupThreadPrefix(CommonConstants systemConstants) {
        String prefix = systemConstants == null ? null : systemConstants.getCache().getMemoryCleanupThreadPrefix();
        if (prefix == null || prefix.trim().isEmpty()) {
            return CommonConstants.Cache.DEFAULT_MEMORY_CLEANUP_THREAD_PREFIX;
        }
        return prefix;
    }

    @Override
    public void close() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
        }
    }

    private static final class CacheItem {
        private final Object value;
        private final Long expireAt;

        private CacheItem(Object value, Long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        private boolean isExpired() {
            return expireAt != null
                    && expireAt != Long.MAX_VALUE
                    && System.nanoTime() > expireAt;
        }
    }

    private static final class CacheItemExpiry implements Expiry<String, CacheItem> {
        @Override
        public long expireAfterCreate(@NonNull String key, @NonNull CacheItem value, long currentTime) {
            return toExpireNanos(value, currentTime);
        }

        @Override
        public long expireAfterUpdate(@NonNull String key, @NonNull CacheItem value, long currentTime, long currentDuration) {
            return toExpireNanos(value, currentTime);
        }

        @Override
        public long expireAfterRead(@NonNull String key, @NonNull CacheItem value, long currentTime, long currentDuration) {
            return currentDuration;
        }

        private long toExpireNanos(CacheItem value, long currentTime) {
            if (value == null || value.expireAt == null || value.expireAt == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long remainingNanos = value.expireAt - currentTime;
            if (remainingNanos <= 0) {
                return 0;
            }
            return remainingNanos;
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private int index = 1;

        private DaemonThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, namePrefix + "-" + index++);
            thread.setDaemon(true);
            return thread;
        }
    }
}
