package com.example.demo.common.cache;

import com.example.demo.common.cache.mapper.CacheMapper;
import com.example.demo.common.config.CommonConstants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 数据库缓存存储实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class DbCacheStore implements CacheStore, AutoCloseable {

    private static final long LIST_MONITOR_IDLE_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private final CacheMapper cacheMapper;
    private final CacheSerializer serializer;
    private final ScheduledExecutorService cleanupExecutor;
    private final long maximumRows;
    private final TransactionTemplate transactionTemplate;
    private final ConcurrentHashMap<String, ListMonitor> listMonitors = new ConcurrentHashMap<>();

    public DbCacheStore(CacheMapper cacheMapper,
                        CacheSerializer serializer,
                        CacheProperties.Db dbProperties,
                        CommonConstants systemConstants,
                        TransactionTemplate transactionTemplate) {
        this.cacheMapper = cacheMapper;
        this.serializer = serializer;
        this.maximumRows = dbProperties == null ? 0 : dbProperties.getMaximumRows();
        this.transactionTemplate = transactionTemplate;
        long cleanupInterval = dbProperties == null ? 0 : dbProperties.getCleanupIntervalSeconds();
        if (cleanupInterval > 0) {
            String prefix = resolveCleanupThreadPrefix(systemConstants);
            this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(prefix));
            this.cleanupExecutor.scheduleAtFixedRate(() -> {
                cleanupExpired();
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
        boolean removed = cacheMapper.deleteById(key) > 0;
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

    @Override
    public Boolean set(String key, Object value) {
        if (key == null) {
            return false;
        }
        set(key, value, null);
        return true;
    }

    @Override
    @Transactional
    public Boolean setNx(String key, Object value) {
        return setIfAbsent(key, value, null);
    }

    @Override
    @Transactional
    public Boolean setXx(String key, Object value) {
        if (key == null) {
            return false;
        }
        CacheEntry locked = lockFresh(key);
        if (locked == null) {
            return false;
        }
        CacheEntry entry = buildEntry(key, value, null);
        return cacheMapper.updateById(entry) > 0;
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
    @Transactional
    public Object getSet(String key, Object value) {
        if (key == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        Object oldValue = locked == null ? null : deserializeValue(locked);
        CacheEntry entry = buildEntry(key, value, null);
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                if (retry != null) {
                    oldValue = deserializeValue(retry);
                    cacheMapper.updateById(entry);
                }
            }
        } else {
            cacheMapper.updateById(entry);
        }
        enforceMaxRows();
        return oldValue;
    }

    @Override
    @Transactional
    public Long incrementBy(String key, long delta) {
        if (key == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        if (locked != null) {
            Object value = deserializeValue(locked);
            long next = parseLong(value) + delta;
            locked.setCacheValue(serializer.serialize(next));
            locked.setValueClass(Long.class.getName());
            cacheMapper.updateById(locked);
            return next;
        }
        CacheEntry entry = buildEntry(key, delta, null);
        int inserted = cacheMapper.insertIgnore(entry);
        if (inserted > 0) {
            enforceMaxRows();
            return delta;
        }
        CacheEntry retry = lockFresh(key);
        if (retry == null) {
            return delta;
        }
        Object value = deserializeValue(retry);
        long next = parseLong(value) + delta;
        retry.setCacheValue(serializer.serialize(next));
        retry.setValueClass(Long.class.getName());
        cacheMapper.updateById(retry);
        return next;
    }

    @Override
    @Transactional
    public Long append(String key, String value) {
        if (key == null) {
            return null;
        }
        String appendValue = value == null ? "" : value;
        CacheEntry locked = lockFresh(key);
        if (locked != null) {
            Object raw = deserializeValue(locked);
            String current = raw == null ? "" : String.valueOf(raw);
            String next = current + appendValue;
            locked.setCacheValue(serializer.serialize(next));
            locked.setValueClass(String.class.getName());
            cacheMapper.updateById(locked);
            return (long) next.length();
        }
        CacheEntry entry = buildEntry(key, appendValue, null);
        int inserted = cacheMapper.insertIgnore(entry);
        if (inserted > 0) {
            enforceMaxRows();
            return (long) appendValue.length();
        }
        CacheEntry retry = lockFresh(key);
        if (retry == null) {
            return (long) appendValue.length();
        }
        Object raw = deserializeValue(retry);
        String current = raw == null ? "" : String.valueOf(raw);
        String next = current + appendValue;
        retry.setCacheValue(serializer.serialize(next));
        retry.setValueClass(String.class.getName());
        cacheMapper.updateById(retry);
        return (long) next.length();
    }

    @Override
    @Transactional
    public Long hset(String key, String field, Object value) {
        if (key == null || field == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        Map<String, Object> map = toHash(locked == null ? null : deserializeValue(locked));
        boolean created = !map.containsKey(field);
        map.put(field, value);
        CacheEntry entry = buildEntry(key, map, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                Map<String, Object> retryMap = toHash(retry == null ? null : deserializeValue(retry));
                boolean retryCreated = !retryMap.containsKey(field);
                retryMap.put(field, value);
                CacheEntry retryEntry = buildEntry(key, retryMap, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                enforceMaxRows();
                return retryCreated ? 1L : 0L;
            }
            enforceMaxRows();
            return created ? 1L : 0L;
        }
        cacheMapper.updateById(entry);
        return created ? 1L : 0L;
    }

    @Override
    public Object hget(String key, String field) {
        if (key == null || field == null) {
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
        Object value = serializer.deserialize(entry.getCacheValue(), entry.getValueClass());
        if (!(value instanceof Map)) {
            return null;
        }
        Map<?, ?> map = (Map<?, ?>) value;
        return map.get(field);
    }

    @Override
    public List<Object> hmget(String key, List<String> fields) {
        if (key == null || fields == null) {
            return null;
        }
        CacheEntry entry = cacheMapper.selectById(key);
        Map<?, ?> map = null;
        if (entry != null) {
            if (isExpired(entry)) {
                cacheMapper.deleteById(key);
            } else {
                Object value = serializer.deserialize(entry.getCacheValue(), entry.getValueClass());
                if (value instanceof Map) {
                    map = (Map<?, ?>) value;
                }
            }
        }
        List<Object> result = new ArrayList<>(fields.size());
        for (String field : fields) {
            result.add(map == null || field == null ? null : map.get(field));
        }
        return result;
    }

    @Override
    @Transactional
    public Long hincrBy(String key, String field, long delta) {
        if (key == null || field == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        Map<String, Object> map = toHash(locked == null ? null : deserializeValue(locked));
        long current = parseLong(map.get(field));
        long next = current + delta;
        map.put(field, next);
        CacheEntry entry = buildEntry(key, map, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                Map<String, Object> retryMap = toHash(retry == null ? null : deserializeValue(retry));
                long retryCurrent = parseLong(retryMap.get(field));
                long retryNext = retryCurrent + delta;
                retryMap.put(field, retryNext);
                CacheEntry retryEntry = buildEntry(key, retryMap, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                return retryNext;
            }
            enforceMaxRows();
            return next;
        }
        cacheMapper.updateById(entry);
        return next;
    }

    @Override
    @Transactional
    public Long hdel(String key, String... fields) {
        if (key == null || fields == null || fields.length == 0) {
            return 0L;
        }
        CacheEntry locked = lockFresh(key);
        if (locked == null) {
            return 0L;
        }
        Map<String, Object> map = toHash(deserializeValue(locked));
        long removed = 0L;
        for (String field : fields) {
            if (field != null && map.remove(field) != null) {
                removed++;
            }
        }
        if (removed == 0L) {
            return 0L;
        }
        if (map.isEmpty()) {
            cacheMapper.deleteById(key);
            return removed;
        }
        CacheEntry entry = buildEntry(key, map, locked.getExpireAt());
        cacheMapper.updateById(entry);
        return removed;
    }

    @Override
    @Transactional
    public Long lpush(String key, List<Object> values) {
        if (key == null || values == null || values.isEmpty()) {
            return 0L;
        }
        CacheEntry locked = lockFresh(key);
        List<Object> list = toList(locked == null ? null : deserializeValue(locked));
        for (Object value : values) {
            list.add(0, value);
        }
        CacheEntry entry = buildEntry(key, list, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                List<Object> retryList = toList(retry == null ? null : deserializeValue(retry));
                for (Object value : values) {
                    retryList.add(0, value);
                }
                CacheEntry retryEntry = buildEntry(key, retryList, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                Long size = (long) retryList.size();
                notifyListWaiters(key);
                return size;
            }
            enforceMaxRows();
        } else {
            cacheMapper.updateById(entry);
        }
        Long size = (long) list.size();
        notifyListWaiters(key);
        return size;
    }

    @Override
    @Transactional
    public Long rpush(String key, List<Object> values) {
        if (key == null || values == null || values.isEmpty()) {
            return 0L;
        }
        CacheEntry locked = lockFresh(key);
        List<Object> list = toList(locked == null ? null : deserializeValue(locked));
        list.addAll(values);
        CacheEntry entry = buildEntry(key, list, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                List<Object> retryList = toList(retry == null ? null : deserializeValue(retry));
                retryList.addAll(values);
                CacheEntry retryEntry = buildEntry(key, retryList, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                Long size = (long) retryList.size();
                notifyListWaiters(key);
                return size;
            }
            enforceMaxRows();
        } else {
            cacheMapper.updateById(entry);
        }
        Long size = (long) list.size();
        notifyListWaiters(key);
        return size;
    }

    @Override
    @Transactional
    public Object lpop(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        if (locked == null) {
            return null;
        }
        Object value = deserializeValue(locked);
        if (!(value instanceof List)) {
            return null;
        }
        List<Object> list = toList(value);
        if (list.isEmpty()) {
            return null;
        }
        Object popped = list.remove(0);
        if (list.isEmpty()) {
            cacheMapper.deleteById(key);
            removeListMonitor(key);
            return popped;
        }
        CacheEntry entry = buildEntry(key, list, locked.getExpireAt());
        cacheMapper.updateById(entry);
        return popped;
    }

    @Override
    @Transactional
    public Object rpop(String key) {
        return rpopInternal(key);
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
            Object value = inTransaction(() -> rpopInternal(key));
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
    @Transactional
    public Long sadd(String key, Collection<Object> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        CacheEntry locked = lockFresh(key);
        LinkedHashSet<Object> set = toSet(locked == null ? null : deserializeValue(locked));
        long added = 0;
        for (Object member : members) {
            if (set.add(member)) {
                added++;
            }
        }
        CacheEntry entry = buildEntry(key, set, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                LinkedHashSet<Object> retrySet = toSet(retry == null ? null : deserializeValue(retry));
                long retryAdded = 0;
                for (Object member : members) {
                    if (retrySet.add(member)) {
                        retryAdded++;
                    }
                }
                CacheEntry retryEntry = buildEntry(key, retrySet, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                return retryAdded;
            }
            enforceMaxRows();
        } else {
            cacheMapper.updateById(entry);
        }
        return added;
    }

    @Override
    @Transactional
    public Long srem(String key, Collection<Object> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        CacheEntry locked = lockFresh(key);
        if (locked == null) {
            return 0L;
        }
        Object value = deserializeValue(locked);
        if (!(value instanceof Collection)) {
            return 0L;
        }
        LinkedHashSet<Object> set = toSet(value);
        long removed = 0;
        for (Object member : members) {
            if (set.remove(member)) {
                removed++;
            }
        }
        if (set.isEmpty()) {
            cacheMapper.deleteById(key);
        } else {
            CacheEntry entry = buildEntry(key, set, locked.getExpireAt());
            cacheMapper.updateById(entry);
        }
        return removed;
    }

    @Override
    public Boolean sismember(String key, Object member) {
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
        Object value = serializer.deserialize(entry.getCacheValue(), entry.getValueClass());
        if (!(value instanceof Collection)) {
            return false;
        }
        return ((Collection<?>) value).contains(member);
    }

    @Override
    @Transactional
    public Long zadd(String key, double score, String member) {
        if (key == null || member == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        Map<String, Double> zset = toZset(locked == null ? null : deserializeValue(locked));
        boolean created = !zset.containsKey(member);
        zset.put(member, score);
        CacheEntry entry = buildEntry(key, zset, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                Map<String, Double> retryZset = toZset(retry == null ? null : deserializeValue(retry));
                boolean retryCreated = !retryZset.containsKey(member);
                retryZset.put(member, score);
                CacheEntry retryEntry = buildEntry(key, retryZset, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                return retryCreated ? 1L : 0L;
            }
            enforceMaxRows();
        } else {
            cacheMapper.updateById(entry);
        }
        return created ? 1L : 0L;
    }

    @Override
    @Transactional
    public Double zincrBy(String key, double delta, String member) {
        if (key == null || member == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        Map<String, Double> zset = toZset(locked == null ? null : deserializeValue(locked));
        double current = zset.getOrDefault(member, 0D);
        double next = current + delta;
        zset.put(member, next);
        CacheEntry entry = buildEntry(key, zset, locked == null ? null : locked.getExpireAt());
        if (locked == null) {
            int inserted = cacheMapper.insertIgnore(entry);
            if (inserted == 0) {
                CacheEntry retry = lockFresh(key);
                Map<String, Double> retryZset = toZset(retry == null ? null : deserializeValue(retry));
                double retryCurrent = retryZset.getOrDefault(member, 0D);
                double retryNext = retryCurrent + delta;
                retryZset.put(member, retryNext);
                CacheEntry retryEntry = buildEntry(key, retryZset, retry == null ? null : retry.getExpireAt());
                cacheMapper.updateById(retryEntry);
                return retryNext;
            }
            enforceMaxRows();
        } else {
            cacheMapper.updateById(entry);
        }
        return next;
    }

    @Override
    @Transactional
    public Long zrem(String key, Collection<String> members) {
        if (key == null || members == null || members.isEmpty()) {
            return 0L;
        }
        CacheEntry locked = lockFresh(key);
        if (locked == null) {
            return 0L;
        }
        Object value = deserializeValue(locked);
        if (!(value instanceof Map)) {
            return 0L;
        }
        Map<String, Double> zset = toZset(value);
        long removed = 0;
        for (String member : members) {
            if (zset.remove(member) != null) {
                removed++;
            }
        }
        if (zset.isEmpty()) {
            cacheMapper.deleteById(key);
        } else {
            CacheEntry entry = buildEntry(key, zset, locked.getExpireAt());
            cacheMapper.updateById(entry);
        }
        return removed;
    }

    @Override
    @Transactional
    public boolean multiSet(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            CacheEntry cacheEntry = buildEntry(entry.getKey(), entry.getValue(), null);
            upsert(cacheEntry);
        }
        enforceMaxRows();
        return true;
    }

    @Override
    public List<Object> multiGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            result.add(get(key));
        }
        return result;
    }

    @Override
    @Transactional
    public boolean tryLock(String key, String token, Duration ttl) {
        if (key == null || token == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return false;
        }
        CacheEntry entry = buildEntry(key, token, computeExpireAt(ttl));
        int inserted = cacheMapper.insertIgnore(entry);
        if (inserted > 0) {
            enforceMaxRows();
            return true;
        }
        CacheEntry locked = cacheMapper.selectForUpdate(key);
        if (locked == null) {
            int retryInserted = cacheMapper.insertIgnore(entry);
            if (retryInserted > 0) {
                enforceMaxRows();
                return true;
            }
            return false;
        }
        if (isExpired(locked)) {
            cacheMapper.updateById(entry);
            enforceMaxRows();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean releaseLock(String key, String token) {
        if (key == null || token == null) {
            return false;
        }
        CacheEntry locked = cacheMapper.selectForUpdate(key);
        if (locked == null) {
            return false;
        }
        if (isExpired(locked)) {
            cacheMapper.deleteById(key);
            return false;
        }
        Object value = serializer.deserialize(locked.getCacheValue(), locked.getValueClass());
        if (!token.equals(String.valueOf(value))) {
            return false;
        }
        return cacheMapper.deleteById(key) > 0;
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

    private void notifyListWaiters(String key) {
        if (key == null) {
            return;
        }
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

    private Object rpopInternal(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry locked = lockFresh(key);
        if (locked == null) {
            return null;
        }
        Object value = deserializeValue(locked);
        if (!(value instanceof List)) {
            return null;
        }
        List<Object> list = toList(value);
        if (list.isEmpty()) {
            return null;
        }
        Object popped = list.remove(list.size() - 1);
        if (list.isEmpty()) {
            cacheMapper.deleteById(key);
            removeListMonitor(key);
            return popped;
        }
        CacheEntry entry = buildEntry(key, list, locked.getExpireAt());
        cacheMapper.updateById(entry);
        return popped;
    }

    private <T> T inTransaction(Supplier<T> action) {
        if (transactionTemplate == null) {
            return action.get();
        }
        return transactionTemplate.execute(status -> action.get());
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

    private CacheEntry lockFresh(String key) {
        CacheEntry entry = cacheMapper.selectForUpdate(key);
        if (entry == null) {
            return null;
        }
        if (isExpired(entry)) {
            cacheMapper.deleteById(key);
            return null;
        }
        return entry;
    }

    private Object deserializeValue(CacheEntry entry) {
        if (entry == null) {
            return null;
        }
        return serializer.deserialize(entry.getCacheValue(), entry.getValueClass());
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

    private static final class ListMonitor {
        private final Object lock = new Object();
        private volatile long lastAccessMillis = System.currentTimeMillis();

        private void touch() {
            lastAccessMillis = System.currentTimeMillis();
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
