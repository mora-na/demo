package com.example.demo.common.mybatis;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.datascope.dto.DataScopeRuleCacheMetricsVO;
import com.example.demo.datascope.entity.DataScopeRule;
import com.example.demo.datascope.service.DataScopeRuleService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于数据库的数据范围规则提供者，支持缓存。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnProperty(prefix = "security.data-scope", name = "source", havingValue = "db", matchIfMissing = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class DbDataScopeRuleProvider implements DataScopeRuleProvider {

    private static final String DATA_SCOPE_RULE_KEY = "data-scope:rules";
    private final ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider;
    private final DataScopeProperties properties;
    private final ObjectProvider<CacheTool> cacheToolProvider;
    private final java.util.concurrent.atomic.AtomicLong localCacheHits = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong localCacheMisses = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong localCacheRefreshes = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong distributedCacheHits = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong distributedCacheMisses = new java.util.concurrent.atomic.AtomicLong();
    private volatile LocalCache localCache;

    /**
     * 构造函数，注入规则服务提供者与缓存配置。
     *
     * @param dataScopeRuleServiceProvider 规则服务提供者
     * @param properties                   数据范围配置
     * @param cacheToolProvider            缓存工具提供者
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DbDataScopeRuleProvider(ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider,
                                   DataScopeProperties properties,
                                   ObjectProvider<CacheTool> cacheToolProvider) {
        this.dataScopeRuleServiceProvider = dataScopeRuleServiceProvider;
        this.properties = properties;
        this.cacheToolProvider = cacheToolProvider;
    }

    /**
     * 获取表列映射，优先读取缓存。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public Map<String, DataScopeRuleDefinition> getRuleMap() {
        long ttlSeconds = properties.getCacheSeconds();
        if (ttlSeconds <= 0) {
            return safeMap(fetchRules());
        }
        long localTtlMillis = resolveLocalCacheMillis();
        if (localTtlMillis > 0) {
            Map<String, DataScopeRuleDefinition> local = readLocalCache();
            if (local != null) {
                localCacheHits.incrementAndGet();
                return local;
            }
            localCacheMisses.incrementAndGet();
        }
        CacheTool cacheTool = cacheToolProvider.getIfAvailable();
        Map<String, DataScopeRuleDefinition> cached = readCache(cacheTool);
        if (cacheTool != null) {
            if (cached != null) {
                distributedCacheHits.incrementAndGet();
            } else {
                distributedCacheMisses.incrementAndGet();
            }
        }
        if (cached != null) {
            updateLocalCache(cached, localTtlMillis);
            return cached;
        }
        Map<String, DataScopeRuleDefinition> fresh = safeMap(fetchRules());
        if (cacheTool != null) {
            cacheTool.set(DATA_SCOPE_RULE_KEY, fresh, Duration.ofSeconds(ttlSeconds));
        }
        updateLocalCache(fresh, localTtlMillis);
        return fresh;
    }

    /**
     * 从数据源拉取启用的规则映射。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Map<String, DataScopeRuleDefinition> fetchRules() {
        DataScopeRuleService service = dataScopeRuleServiceProvider.getIfAvailable();
        if (service == null) {
            return Collections.emptyMap();
        }
        Map<String, DataScopeRule> rules = service.getEnabledRules();
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DataScopeRuleDefinition> result = new HashMap<>();
        for (Map.Entry<String, DataScopeRule> entry : rules.entrySet()) {
            String scopeKey = entry.getKey();
            DataScopeRule rule = entry.getValue();
            if (rule == null) {
                continue;
            }
            DataScopeRuleDefinition definition = new DataScopeRuleDefinition();
            definition.setScopeKey(scopeKey);
            definition.setTableName(rule.getTableName());
            definition.setTableAlias(rule.getTableAlias());
            definition.setDeptColumn(rule.getDeptColumn());
            definition.setUserColumn(rule.getUserColumn());
            definition.setFilterType(rule.getFilterType());
            definition.setStatus(rule.getStatus());
            result.put(scopeKey, definition);
        }
        return result;
    }

    /**
     * 对映射进行空安全处理与拷贝。
     *
     * @param map 原始映射
     * @return 安全映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Map<String, DataScopeRuleDefinition> safeMap(Map<String, DataScopeRuleDefinition> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(map);
    }

    private long resolveLocalCacheMillis() {
        return Math.max(0L, properties.getLocalCacheMillis());
    }

    private Map<String, DataScopeRuleDefinition> readLocalCache() {
        LocalCache cache = localCache;
        if (cache == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (cache.getExpireAtMillis() > 0 && cache.getExpireAtMillis() < now) {
            localCache = null;
            return null;
        }
        return safeMap(cache.getValue());
    }

    private void updateLocalCache(Map<String, DataScopeRuleDefinition> value, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        long updatedAt = System.currentTimeMillis();
        long expireAt = updatedAt + ttlMillis;
        localCache = new LocalCache(safeMap(value), expireAt, updatedAt);
        localCacheRefreshes.incrementAndGet();
    }

    public long getLocalCacheHits() {
        return localCacheHits.get();
    }

    public long getLocalCacheMisses() {
        return localCacheMisses.get();
    }

    public long getLocalCacheRefreshes() {
        return localCacheRefreshes.get();
    }

    public long getDistributedCacheHits() {
        return distributedCacheHits.get();
    }

    public long getDistributedCacheMisses() {
        return distributedCacheMisses.get();
    }

    public DataScopeRuleCacheMetricsVO snapshotCacheMetrics() {
        DataScopeRuleCacheMetricsVO metrics = new DataScopeRuleCacheMetricsVO();
        metrics.setCacheKey(DATA_SCOPE_RULE_KEY);
        metrics.setDistributedCacheEnabled(properties.getCacheSeconds() > 0);
        metrics.setDistributedCacheTtlSeconds(properties.getCacheSeconds());
        metrics.setDistributedCacheHits(getDistributedCacheHits());
        metrics.setDistributedCacheMisses(getDistributedCacheMisses());
        metrics.setDistributedCacheHitRate(resolveHitRate(metrics.getDistributedCacheHits(), metrics.getDistributedCacheMisses()));
        long localTtlMillis = resolveLocalCacheMillis();
        metrics.setLocalCacheEnabled(localTtlMillis > 0);
        metrics.setLocalCacheTtlMillis(localTtlMillis);
        metrics.setLocalCacheHits(getLocalCacheHits());
        metrics.setLocalCacheMisses(getLocalCacheMisses());
        metrics.setLocalCacheRefreshes(getLocalCacheRefreshes());
        metrics.setLocalCacheHitRate(resolveHitRate(metrics.getLocalCacheHits(), metrics.getLocalCacheMisses()));
        LocalCache cache = localCache;
        if (cache != null && cache.getValue() != null) {
            metrics.setLocalCacheSize(cache.getValue().size());
            metrics.setLocalCacheExpireAtMillis(cache.getExpireAtMillis());
            metrics.setLocalCacheUpdatedAtMillis(cache.getUpdatedAtMillis());
        } else {
            metrics.setLocalCacheExpireAtMillis(-1L);
            metrics.setLocalCacheUpdatedAtMillis(-1L);
        }
        return metrics;
    }

    private double resolveHitRate(long hits, long misses) {
        long total = hits + misses;
        if (total <= 0) {
            return 0d;
        }
        return hits * 1d / total;
    }

    /**
     * 读取缓存中的映射。
     *
     * @return 表列映射，未命中返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @SuppressWarnings("unchecked")
    private Map<String, DataScopeRuleDefinition> readCache(CacheTool cacheTool) {
        if (cacheTool == null) {
            return null;
        }
        Object value = cacheTool.get(DATA_SCOPE_RULE_KEY);
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            return new HashMap<>((Map<String, DataScopeRuleDefinition>) value);
        }
        return null;
    }

    private static class LocalCache {
        private final Map<String, DataScopeRuleDefinition> value;
        private final long expireAtMillis;
        private final long updatedAtMillis;

        private LocalCache(Map<String, DataScopeRuleDefinition> value, long expireAtMillis, long updatedAtMillis) {
            this.value = value;
            this.expireAtMillis = expireAtMillis;
            this.updatedAtMillis = updatedAtMillis;
        }

        private Map<String, DataScopeRuleDefinition> getValue() {
            return value;
        }

        private long getExpireAtMillis() {
            return expireAtMillis;
        }

        private long getUpdatedAtMillis() {
            return updatedAtMillis;
        }
    }
}
