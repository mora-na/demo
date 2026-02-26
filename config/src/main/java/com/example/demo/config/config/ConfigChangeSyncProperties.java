package com.example.demo.config.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置变更跨节点同步配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "config.change-sync")
public class ConfigChangeSyncProperties {

    /**
     * 是否启用跨节点同步。
     */
    private boolean enabled = true;

    /**
     * 是否启用变更流水拉取。
     */
    private boolean pullEnabled = true;

    /**
     * 变更事件队列 Key。
     */
    private String queueKey = "config:change:queue";

    /**
     * 轮询间隔（毫秒）。
     */
    private long pollIntervalMillis = 1000L;

    /**
     * 单次拉取的最大事件数。
     */
    private int maxBatchSize = 200;

    /**
     * 队列 Key 的过期时间（秒），<=0 表示不设置。
     */
    private int queueTtlSeconds = 300;

    /**
     * 游标 Key 前缀（用于变更流水拉取）。
     */
    private String cursorKeyPrefix = "config:change:cursor:";

    /**
     * 游标过期时间（秒），<=0 表示不设置。
     */
    private int cursorTtlSeconds = 86400;

    /**
     * 兜底全量刷新间隔（秒），<=0 表示关闭。
     */
    private int fallbackRefreshIntervalSeconds = 0;

    /**
     * 已处理变更缓存最大条数（防止本地缓存无限增长）。
     */
    private int processedCacheMaxSize = 10000;
}
