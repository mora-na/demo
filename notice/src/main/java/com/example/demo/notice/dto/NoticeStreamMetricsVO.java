package com.example.demo.notice.dto;

import lombok.Data;

/**
 * SSE 流状态指标快照。
 */
@Data
public class NoticeStreamMetricsVO {
    private long totalConnections;
    private int activeUsers;
    private long latestCacheSize;
    private long connectionCounterSize;
    private int latestLimit;
    private int maxTotalConnections;
    private int maxConnectionsPerUser;
    private long latestCacheMaxSize;
    private int latestCacheExpireMinutes;
    private boolean autoDegradeEnabled;
    private boolean degraded;
    private double degradeConnectionRatio;
    private double degradeCacheRatio;
}
