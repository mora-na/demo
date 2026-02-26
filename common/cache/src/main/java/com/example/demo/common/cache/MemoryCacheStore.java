package com.example.demo.common.cache;

import com.example.demo.common.config.CommonConstants;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
    private static final long LIST_MONITOR_IDLE_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private final Cache<String, CacheItem> cache;
    private final ScheduledExecutorService cleanupExecutor;
    private final long maximumWeightBytes;
    private final ConcurrentHashMap<String, ListMonitor> listMonitors = new ConcurrentHashMap<>();

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
            this.cleanupExecutor.scheduleAtFixedRate(() -> {
                cache.cleanUp();
                cleanupIdleListMonitors();
            }, cleanupInterval, cleanupInterval, TimeUnit.SECONDS);
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
            removeListMonitor(key);
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
        boolean removed = cache.asMap().remove(key) != null;
        if (removed) {
            removeListMonitor(key);
        }
        return removed;
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

    @Override
    public Boolean set(String key, Object value) {
        if (key == null) {
            return false;
        }
        set(key, value, null);
        return true;
    }

    @Override
    public Boolean setNx(String key, Object value) {
        return setIfAbsent(key, value, null);
    }

    @Override
    public Boolean setXx(String key, Object value) {
        if (key == null) {
            return false;
        }
        if (maximumWeightBytes > 0 && estimateWeightBytes(key, value) > maximumWeightBytes) {
            return false;
        }
        AtomicBoolean updated = new AtomicBoolean(false);
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return null;
            }
            updated.set(true);
            return new CacheItem(value, null);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return updated.get();
    }

    @Override
    public Boolean setEx(String key, Object value, Duration ttl) {
        if (key == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return false;
        }
        set(key, value, ttl);
        return true;
    }

    @Override
    public Object getSet(String key, Object value) {
        if (key == null) {
            return null;
        }
        AtomicReference<Object> oldValue = new AtomicReference<>();
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                oldValue.set(null);
            } else {
                oldValue.set(existing.value);
            }
            return new CacheItem(value, null);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return oldValue.get();
    }

    @Override
    public Long incrementBy(String key, long delta) {
        if (key == null) {
            return null;
        }
        AtomicLong next = new AtomicLong(0);
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                next.set(delta);
                return new CacheItem(delta, null);
            }
            long current = parseLong(existing.value);
            long updated = current + delta;
            next.set(updated);
            return new CacheItem(updated, existing.expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return next.get();
    }

    @Override
    public Long append(String key, String value) {
        if (key == null) {
            return null;
        }
        String appendValue = value == null ? "" : value;
        AtomicLong length = new AtomicLong(0);
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                String nextValue = appendValue;
                length.set(nextValue.length());
                return new CacheItem(nextValue, null);
            }
            String current = existing.value == null ? "" : String.valueOf(existing.value);
            String nextValue = current + appendValue;
            length.set(nextValue.length());
            return new CacheItem(nextValue, existing.expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return length.get();
    }

    @Override
    public Long hset(String key, String field, Object value) {
        if (key == null || field == null) {
            return null;
        }
        AtomicBoolean created = new AtomicBoolean(false);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            Map<String, Object> map = toHash(item == null ? null : item.value);
            if (!map.containsKey(field)) {
                created.set(true);
            }
            map.put(field, value);
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(map, expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return created.get() ? 1L : 0L;
    }

    @Override
    public Object hget(String key, String field) {
        if (key == null || field == null) {
            return null;
        }
        CacheItem item = getFreshItem(key);
        if (item == null) {
            return null;
        }
        if (!(item.value instanceof Map)) {
            return null;
        }
        Map<?, ?> map = (Map<?, ?>) item.value;
        return map.get(field);
    }

    @Override
    public List<Object> hmget(String key, List<String> fields) {
        if (key == null || fields == null) {
            return null;
        }
        CacheItem item = getFreshItem(key);
        Map<?, ?> map = item != null && item.value instanceof Map ? (Map<?, ?>) item.value : null;
        List<Object> result = new ArrayList<>(fields.size());
        for (String field : fields) {
            if (map == null || field == null) {
                result.add(null);
                continue;
            }
            result.add(map.get(field));
        }
        return result;
    }

    @Override
    public Long hincrBy(String key, String field, long delta) {
        if (key == null || field == null) {
            return null;
        }
        AtomicLong next = new AtomicLong(0);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            Map<String, Object> map = toHash(item == null ? null : item.value);
            long current = parseLong(map.get(field));
            long updated = current + delta;
            map.put(field, updated);
            next.set(updated);
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(map, expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return next.get();
    }

    @Override
    public Long lpush(String key, List<Object> values) {
        if (key == null || values == null || values.isEmpty()) {
            return 0L;
        }
        AtomicLong size = new AtomicLong(0);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            List<Object> list = toList(item == null ? null : item.value);
            for (Object value : values) {
                list.add(0, value);
            }
            size.set(list.size());
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(list, expireAt);
        });
        notifyListWaiters(key);
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return size.get();
    }

    @Override
    public Long rpush(String key, List<Object> values) {
        if (key == null || values == null || values.isEmpty()) {
            return 0L;
        }
        AtomicLong size = new AtomicLong(0);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            List<Object> list = toList(item == null ? null : item.value);
            list.addAll(values);
            size.set(list.size());
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(list, expireAt);
        });
        notifyListWaiters(key);
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return size.get();
    }

    @Override
    public Object lpop(String key) {
        if (key == null) {
            return null;
        }
        AtomicReference<Object> popped = new AtomicReference<>();
        AtomicBoolean removed = new AtomicBoolean(false);
        cache.asMap().computeIfPresent(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            if (item == null || !(item.value instanceof List)) {
                return item;
            }
            List<Object> list = toList(item.value);
            if (list.isEmpty()) {
                removed.set(true);
                return null;
            }
            popped.set(list.remove(0));
            if (list.isEmpty()) {
                removed.set(true);
                return null;
            }
            return new CacheItem(list, item.expireAt);
        });
        if (removed.get()) {
            removeListMonitor(key);
        }
        return popped.get();
    }

    @Override
    public Object rpop(String key) {
        if (key == null) {
            return null;
        }
        AtomicReference<Object> popped = new AtomicReference<>();
        AtomicBoolean removed = new AtomicBoolean(false);
        cache.asMap().computeIfPresent(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            if (item == null || !(item.value instanceof List)) {
                return item;
            }
            List<Object> list = toList(item.value);
            if (list.isEmpty()) {
                removed.set(true);
                return null;
            }
            popped.set(list.remove(list.size() - 1));
            if (list.isEmpty()) {
                removed.set(true);
                return null;
            }
            return new CacheItem(list, item.expireAt);
        });
        if (removed.get()) {
            removeListMonitor(key);
        }
        return popped.get();
    }

    @Override
    public Object brpop(String key, long timeout, TimeUnit unit) {
        if (key == null || unit == null) {
            return null;
        }
        long timeoutMillis = unit.toMillis(timeout);
        boolean noTimeout = timeoutMillis <= 0;
        long deadline = noTimeout ? Long.MAX_VALUE : System.currentTimeMillis() + timeoutMillis;
        ListMonitor monitor = listMonitors.computeIfAbsent(key, ignored -> new ListMonitor());
        monitor.touch();
        while (true) {
            Object value = rpop(key);
            if (value != null) {
                return value;
            }
            long remaining = deadline - System.currentTimeMillis();
            if (!noTimeout && remaining <= 0) {
                return null;
            }
            synchronized (monitor.lock) {
                try {
                    long waitMillis = noTimeout ? 1000L : Math.min(remaining, 1000L);
                    monitor.lock.wait(waitMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
    }

    @Override
    public Long sadd(String key, Collection<Object> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        AtomicLong added = new AtomicLong(0);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            LinkedHashSet<Object> set = toSet(item == null ? null : item.value);
            for (Object member : members) {
                if (set.add(member)) {
                    added.incrementAndGet();
                }
            }
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(set, expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return added.get();
    }

    @Override
    public Long srem(String key, Collection<Object> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        AtomicLong removed = new AtomicLong(0);
        cache.asMap().computeIfPresent(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            if (item == null || !(item.value instanceof Collection)) {
                return item;
            }
            LinkedHashSet<Object> set = toSet(item.value);
            for (Object member : members) {
                if (set.remove(member)) {
                    removed.incrementAndGet();
                }
            }
            if (set.isEmpty()) {
                return null;
            }
            return new CacheItem(set, item.expireAt);
        });
        return removed.get();
    }

    @Override
    public Boolean sismember(String key, Object member) {
        if (key == null) {
            return false;
        }
        CacheItem item = getFreshItem(key);
        if (item == null || !(item.value instanceof Collection)) {
            return false;
        }
        return ((Collection<?>) item.value).contains(member);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        if (key == null || member == null) {
            return null;
        }
        AtomicBoolean created = new AtomicBoolean(false);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            Map<String, Double> zset = toZset(item == null ? null : item.value);
            if (!zset.containsKey(member)) {
                created.set(true);
            }
            zset.put(member, score);
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(zset, expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return created.get() ? 1L : 0L;
    }

    @Override
    public Double zincrBy(String key, double delta, String member) {
        if (key == null || member == null) {
            return null;
        }
        AtomicReference<Double> next = new AtomicReference<>(0D);
        cache.asMap().compute(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            Map<String, Double> zset = toZset(item == null ? null : item.value);
            double current = zset.getOrDefault(member, 0D);
            double updated = current + delta;
            zset.put(member, updated);
            next.set(updated);
            Long expireAt = item == null ? null : item.expireAt;
            return new CacheItem(zset, expireAt);
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return next.get();
    }

    @Override
    public Long zrem(String key, Collection<String> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        AtomicLong removed = new AtomicLong(0);
        cache.asMap().computeIfPresent(key, (k, existing) -> {
            CacheItem item = normalizeItem(existing);
            if (item == null || !(item.value instanceof Map)) {
                return item;
            }
            Map<String, Double> zset = toZset(item.value);
            for (String member : members) {
                if (zset.remove(member) != null) {
                    removed.incrementAndGet();
                }
            }
            if (zset.isEmpty()) {
                return null;
            }
            return new CacheItem(zset, item.expireAt);
        });
        return removed.get();
    }

    @Override
    public boolean multiSet(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            set(entry.getKey(), entry.getValue(), null);
        }
        return true;
    }

    @Override
    public boolean tryLock(String key, String token, Duration ttl) {
        if (key == null || token == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return false;
        }
        if (maximumWeightBytes > 0 && estimateWeightBytes(key, token) > maximumWeightBytes) {
            return false;
        }
        Long expireAt = computeExpireAtNanos(ttl);
        if (expireAt == null) {
            return false;
        }
        AtomicBoolean acquired = new AtomicBoolean(false);
        cache.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                acquired.set(true);
                return new CacheItem(token, expireAt);
            }
            return existing;
        });
        if (maximumWeightBytes > 0) {
            cache.cleanUp();
        }
        return acquired.get();
    }

    @Override
    public boolean releaseLock(String key, String token) {
        if (key == null || token == null) {
            return false;
        }
        AtomicBoolean released = new AtomicBoolean(false);
        cache.asMap().computeIfPresent(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return null;
            }
            if (token.equals(String.valueOf(existing.value))) {
                released.set(true);
                return null;
            }
            return existing;
        });
        return released.get();
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
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0D;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0D;
        }
    }

    private CacheItem normalizeItem(CacheItem existing) {
        if (existing == null) {
            return null;
        }
        if (existing.isExpired()) {
            return null;
        }
        return existing;
    }

    private CacheItem getFreshItem(String key) {
        CacheItem item = cache.getIfPresent(key);
        if (item == null) {
            return null;
        }
        if (item.isExpired()) {
            cache.invalidate(key);
            return null;
        }
        return item;
    }

    private Map<String, Object> toHash(Object value) {
        if (!(value instanceof Map)) {
            return new LinkedHashMap<>();
        }
        Map<?, ?> raw = (Map<?, ?>) value;
        if (raw.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return map;
    }

    private List<Object> toList(Object value) {
        if (!(value instanceof List)) {
            return new ArrayList<>();
        }
        List<?> raw = (List<?>) value;
        if (raw.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(raw);
    }

    private LinkedHashSet<Object> toSet(Object value) {
        LinkedHashSet<Object> set = new LinkedHashSet<>();
        if (value instanceof Collection) {
            set.addAll((Collection<?>) value);
        }
        return set;
    }

    private Map<String, Double> toZset(Object value) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (!(value instanceof Map)) {
            return map;
        }
        Map<?, ?> raw = (Map<?, ?>) value;
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String member = String.valueOf(entry.getKey());
            map.put(member, parseDouble(entry.getValue()));
        }
        return map;
    }

    private void notifyListWaiters(String key) {
        ListMonitor monitor = listMonitors.get(key);
        if (monitor == null) {
            return;
        }
        monitor.touch();
        synchronized (monitor.lock) {
            monitor.lock.notifyAll();
        }
    }

    private void removeListMonitor(String key) {
        if (key == null) {
            return;
        }
        ListMonitor monitor = listMonitors.remove(key);
        if (monitor == null) {
            return;
        }
        synchronized (monitor.lock) {
            monitor.lock.notifyAll();
        }
    }

    private void cleanupIdleListMonitors() {
        if (listMonitors.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ListMonitor> entry : listMonitors.entrySet()) {
            ListMonitor monitor = entry.getValue();
            if (monitor == null) {
                listMonitors.remove(entry.getKey(), null);
                continue;
            }
            if (now - monitor.lastAccessMillis > LIST_MONITOR_IDLE_MILLIS) {
                listMonitors.remove(entry.getKey(), monitor);
            }
        }
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

    private static final class ListMonitor {
        private final Object lock = new Object();
        private volatile long lastAccessMillis = System.currentTimeMillis();

        private void touch() {
            lastAccessMillis = System.currentTimeMillis();
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
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable, namePrefix + "-" + index++);
            thread.setDaemon(true);
            return thread;
        }
    }
}
