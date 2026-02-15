package com.example.demo.common.cache;

import com.example.demo.common.cache.mapper.CacheMapper;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.notice.mapper.NoticeRecipientMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 数据库缓存存储实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class DbCacheStore implements CacheStore, AutoCloseable {

    private final CacheMapper cacheMapper;
    private final CacheSerializer serializer;
    private final ScheduledExecutorService cleanupExecutor;
    private final long maximumRows;

    public DbCacheStore(CacheMapper cacheMapper, CacheSerializer serializer, CacheProperties.Db dbProperties, CommonConstants systemConstants) {
        this.cacheMapper = cacheMapper;
        this.serializer = serializer;
        this.maximumRows = dbProperties == null ? 0 : dbProperties.getMaximumRows();
        long cleanupInterval = dbProperties == null ? 0 : dbProperties.getCleanupIntervalSeconds();
        if (cleanupInterval > 0) {
            String prefix = resolveCleanupThreadPrefix(systemConstants);
            this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(prefix));
            this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpired, cleanupInterval, cleanupInterval, TimeUnit.SECONDS);
        } else {
            this.cleanupExecutor = null;
        }
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        if (key == null) {
            return;
        }
        CacheEntry entry = buildEntry(key, value, computeExpireAt(ttl));
        upsert(entry);
        enforceMaxRows();
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry entry = cacheMapper.selectById(key);
        if (entry == null) {
            return null;
        }
        if (isExpired(entry)) {
            cacheMapper.deleteById(key);
            return null;
        }
        return serializer.deserialize(entry.getCacheValue(), entry.getValueClass());
    }

    @Override
    @Transactional
    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        if (key == null) {
            return false;
        }
        CacheEntry entry = buildEntry(key, value, computeExpireAt(ttl));
        int inserted = cacheMapper.insertIgnore(entry);
        if (inserted > 0) {
            enforceMaxRows();
            return true;
        }
        int updated = cacheMapper.updateIfExpired(entry, System.currentTimeMillis());
        if (updated > 0) {
            enforceMaxRows();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Long increment(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry existing = cacheMapper.selectForUpdate(key);
        if (existing != null && !isExpired(existing)) {
            Object value = serializer.deserialize(existing.getCacheValue(), existing.getValueClass());
            long next = parseLong(value) + 1;
            existing.setCacheValue(serializer.serialize(next));
            existing.setValueClass(Long.class.getName());
            cacheMapper.updateById(existing);
            return next;
        }
        if (existing != null && isExpired(existing)) {
            cacheMapper.deleteById(key);
        }
        CacheEntry entry = buildEntry(key, 1L, null);
        int inserted = cacheMapper.insertIgnore(entry);
        if (inserted > 0) {
            enforceMaxRows();
            return 1L;
        }
        CacheEntry locked = cacheMapper.selectForUpdate(key);
        if (locked == null) {
            int retryInserted = cacheMapper.insertIgnore(entry);
            if (retryInserted > 0) {
                enforceMaxRows();
                return 1L;
            }
            return 1L;
        }
        if (isExpired(locked)) {
            cacheMapper.updateById(entry);
            enforceMaxRows();
            return 1L;
        }
        Object value = serializer.deserialize(locked.getCacheValue(), locked.getValueClass());
        long next = parseLong(value) + 1;
        locked.setCacheValue(serializer.serialize(next));
        locked.setValueClass(Long.class.getName());
        cacheMapper.updateById(locked);
        return next;
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return cacheMapper.deleteById(key) > 0;
    }

    @Override
    public boolean hasKey(String key) {
        if (key == null) {
            return false;
        }
        CacheEntry entry = cacheMapper.selectById(key);
        if (entry == null) {
            return false;
        }
        if (isExpired(entry)) {
            cacheMapper.deleteById(key);
            return false;
        }
        return true;
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        if (key == null) {
            return false;
        }
        CacheEntry entry = cacheMapper.selectById(key);
        if (entry == null || isExpired(entry)) {
            if (entry != null) {
                cacheMapper.deleteById(key);
            }
            return false;
        }
        long expireAt = computeExpireAt(ttl);
        if (expireAt == 0L) {
            return false;
        }
        entry.setExpireAt(expireAt);
        return cacheMapper.updateById(entry) > 0;
    }

    @Override
    public long getExpire(String key, TimeUnit unit) {
        if (key == null) {
            return -2;
        }
        CacheEntry entry = cacheMapper.selectById(key);
        if (entry == null) {
            return -2;
        }
        if (isExpired(entry)) {
            cacheMapper.deleteById(key);
            return -2;
        }
        if (entry.getExpireAt() == null) {
            return -1;
        }
        long remainingMillis = entry.getExpireAt() - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            cacheMapper.deleteById(key);
            return -2;
        }
        return unit.convert(remainingMillis, TimeUnit.MILLISECONDS);
    }

    private void upsert(CacheEntry entry) {
        int inserted = cacheMapper.insertIgnore(entry);
        if (inserted == 0) {
            cacheMapper.updateById(entry);
        }
    }

    private void cleanupExpired() {
        cacheMapper.deleteExpired(System.currentTimeMillis());
    }

    private void enforceMaxRows() {
        if (maximumRows <= 0) {
            return;
        }
        cacheMapper.deleteExpired(System.currentTimeMillis());
        Long count = cacheMapper.selectCount(null);
        if (count == null || count <= maximumRows) {
            return;
        }
        long overflow = count - maximumRows;
        int limit = overflow > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) overflow;
        List<String> keys = cacheMapper.selectKeysForEviction(limit);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        cacheMapper.deleteBatchIds(keys);
    }

    private CacheEntry buildEntry(String key, Object value, Long expireAt) {
        CacheEntry entry = new CacheEntry();
        entry.setCacheKey(key);
        entry.setCacheValue(serializer.serialize(value));
        entry.setValueClass(value == null ? null : value.getClass().getName());
        entry.setExpireAt(expireAt == null || expireAt == 0L ? null : expireAt);
        return entry;
    }

    private Long computeExpireAt(Duration ttl) {
        if (ttl == null) {
            return null;
        }
        long millis = ttl.toMillis();
        if (millis <= 0) {
            return null;
        }
        return System.currentTimeMillis() + millis;
    }

    private boolean isExpired(CacheEntry entry) {
        return entry.getExpireAt() != null && System.currentTimeMillis() > entry.getExpireAt();
    }

    private long parseLong(Object value) {
        return NoticeRecipientMapper.toLong(value);
    }

    private String resolveCleanupThreadPrefix(CommonConstants systemConstants) {
        String prefix = systemConstants == null ? null : systemConstants.getCache().getDbCleanupThreadPrefix();
        if (prefix == null || prefix.trim().isEmpty()) {
            return CommonConstants.Cache.DEFAULT_DB_CLEANUP_THREAD_PREFIX;
        }
        return prefix;
    }

    @Override
    public void close() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
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
