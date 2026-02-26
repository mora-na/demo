package com.example.demo.datascope.dto;

import lombok.Data;

/**
 * 数据范围规则缓存指标快照。
 */
@Data
public class DataScopeRuleCacheMetricsVO {
    private String cacheKey;
    private boolean distributedCacheEnabled;
    private long distributedCacheTtlSeconds;
    private long distributedCacheHits;
    private long distributedCacheMisses;
    private double distributedCacheHitRate;
    private boolean localCacheEnabled;
    private long localCacheTtlMillis;
    private int localCacheSize;
    private long localCacheExpireAtMillis;
    private long localCacheUpdatedAtMillis;
    private long localCacheHits;
    private long localCacheMisses;
    private long localCacheRefreshes;
    private double localCacheHitRate;
}
