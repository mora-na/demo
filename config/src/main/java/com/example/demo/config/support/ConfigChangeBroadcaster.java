package com.example.demo.config.support;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.cluster.NodeIdProvider;
import com.example.demo.config.api.event.ConfigChangeEvent;
import com.example.demo.config.config.ConfigChangeSyncProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 配置变更跨节点广播。
 */
@Component
public class ConfigChangeBroadcaster {

    private final CacheTool cacheTool;
    private final ConfigChangeSyncProperties properties;
    private final NodeIdProvider nodeIdProvider;

    public ConfigChangeBroadcaster(CacheTool cacheTool,
                                   ConfigChangeSyncProperties properties,
                                   NodeIdProvider nodeIdProvider) {
        this.cacheTool = cacheTool;
        this.properties = properties;
        this.nodeIdProvider = nodeIdProvider;
    }

    public void broadcast(ConfigChangeEvent event) {
        if (event == null || properties == null || !properties.isEnabled()) {
            return;
        }
        if (properties.isPullEnabled()) {
            return;
        }
        String group = event.getGroup();
        String key = event.getKey();
        if (group == null || key == null) {
            return;
        }
        ConfigChangeSignal signal = new ConfigChangeSignal(
                group,
                key,
                event.getVersion(),
                event.isHotUpdate(),
                nodeIdProvider == null ? null : nodeIdProvider.get(),
                System.currentTimeMillis());
        cacheTool.lpush(resolveQueueKey(), signal);
        int ttlSeconds = properties.getQueueTtlSeconds();
        if (ttlSeconds > 0) {
            cacheTool.expire(resolveQueueKey(), Duration.ofSeconds(ttlSeconds));
        }
    }

    private String resolveQueueKey() {
        String key = properties.getQueueKey();
        return key == null || key.trim().isEmpty() ? "config:change:queue" : key.trim();
    }
}
