package com.example.demo.config.support;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.cluster.NodeIdProvider;
import com.example.demo.config.api.event.ConfigChangeEvent;
import com.example.demo.config.config.ConfigChangeSyncProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 配置变更跨节点订阅。
 */
@Slf4j
@Component
public class ConfigChangeSubscriber {

    private final CacheTool cacheTool;
    private final ConfigChangePublisher changePublisher;
    private final ConfigChangeSyncProperties properties;
    private final NodeIdProvider nodeIdProvider;
    private final ObjectMapper objectMapper;
    private final ConfigBindingManager bindingManager;
    private final ConfigChangeLogService changeLogService;
    private final LruMap<String, Integer> lastProcessed;
    private ScheduledExecutorService scheduler;

    public ConfigChangeSubscriber(CacheTool cacheTool,
                                  ConfigChangePublisher changePublisher,
                                  ConfigChangeSyncProperties properties,
                                  NodeIdProvider nodeIdProvider,
                                  ObjectMapper objectMapper,
                                  ConfigBindingManager bindingManager,
                                  ConfigChangeLogService changeLogService) {
        this.cacheTool = cacheTool;
        this.changePublisher = changePublisher;
        this.properties = properties;
        this.nodeIdProvider = nodeIdProvider;
        this.objectMapper = objectMapper;
        this.bindingManager = bindingManager;
        this.changeLogService = changeLogService;
        int maxSize = properties == null ? 10000 : properties.getProcessedCacheMaxSize();
        this.lastProcessed = new LruMap<>(Math.max(1000, maxSize));
    }

    @PostConstruct
    public void start() {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        long interval = Math.max(200L, properties.getPollIntervalMillis());
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "config-change-sync");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::poll, interval, interval, TimeUnit.MILLISECONDS);
        int fallbackSeconds = properties.getFallbackRefreshIntervalSeconds();
        if (fallbackSeconds > 0) {
            long fallbackMillis = Math.max(1000L, fallbackSeconds * 1000L);
            scheduler.scheduleWithFixedDelay(this::fallbackRefresh, fallbackMillis, fallbackMillis, TimeUnit.MILLISECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void poll() {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        if (properties.isPullEnabled()) {
            pollChangeLogs();
            return;
        }
        pollQueue();
    }

    private void pollChangeLogs() {
        if (changeLogService == null) {
            return;
        }
        int maxBatch = Math.max(1, properties.getMaxBatchSize());
        long lastId = resolveCursor();
        List<com.example.demo.config.entity.SysConfigChangeLog> logs =
                changeLogService.listAfterId(lastId, maxBatch);
        if (logs == null || logs.isEmpty()) {
            return;
        }
        for (com.example.demo.config.entity.SysConfigChangeLog logItem : logs) {
            if (logItem == null || logItem.getId() == null) {
                continue;
            }
            if (logItem.getNodeId() != null && nodeIdProvider != null
                    && logItem.getNodeId().equals(nodeIdProvider.get())) {
                lastId = logItem.getId();
                continue;
            }
            if (isStale(logItem.getConfigGroup(), logItem.getConfigKey(), logItem.getConfigVersion())) {
                lastId = logItem.getId();
                continue;
            }
            if (logItem.getHotUpdate() != null && logItem.getHotUpdate() == 1) {
                ConfigChangeEvent event = new ConfigChangeEvent(
                        logItem.getConfigGroup(),
                        logItem.getConfigKey(),
                        null,
                        null,
                        com.example.demo.config.api.enums.ConfigValueType.from(logItem.getConfigType()),
                        logItem.getConfigVersion(),
                        true);
                try {
                    changePublisher.publish(event);
                    markProcessed(logItem.getConfigGroup(), logItem.getConfigKey(), logItem.getConfigVersion());
                } catch (Exception ex) {
                    break;
                }
            }
            lastId = logItem.getId();
        }
        persistCursor(lastId);
    }

    private void pollQueue() {
        int maxBatch = Math.max(1, properties.getMaxBatchSize());
        String queueKey = resolveQueueKey();
        for (int i = 0; i < maxBatch; i++) {
            Object raw = cacheTool.rpop(queueKey);
            if (raw == null) {
                return;
            }
            ConfigChangeSignal signal = convert(raw);
            if (signal == null) {
                continue;
            }
            if (isSelfSignal(signal)) {
                continue;
            }
            if (isStale(signal.getGroup(), signal.getKey(), signal.getVersion())) {
                continue;
            }
            ConfigChangeEvent event = new ConfigChangeEvent(
                    signal.getGroup(),
                    signal.getKey(),
                    null,
                    null,
                    null,
                    signal.getVersion(),
                    signal.isHotUpdate());
            try {
                changePublisher.publish(event);
                markProcessed(signal.getGroup(), signal.getKey(), signal.getVersion());
            } catch (Exception ex) {
                cacheTool.lpush(queueKey, signal);
                log.debug("Config change replay failed, requeued. group={}, key={}", signal.getGroup(), signal.getKey(), ex);
                return;
            }
        }
    }

    private boolean isSelfSignal(ConfigChangeSignal signal) {
        if (signal == null || nodeIdProvider == null) {
            return false;
        }
        String self = nodeIdProvider.get();
        String source = signal.getNodeId();
        return self != null && source != null && self.equals(source);
    }

    private ConfigChangeSignal convert(Object raw) {
        if (raw instanceof ConfigChangeSignal) {
            return (ConfigChangeSignal) raw;
        }
        if (raw instanceof Map) {
            return objectMapper.convertValue(raw, ConfigChangeSignal.class);
        }
        if (raw instanceof String) {
            try {
                return objectMapper.readValue((String) raw, ConfigChangeSignal.class);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    private boolean isStale(String group, String key, Integer version) {
        if (version == null) {
            return false;
        }
        String cacheKey = buildProcessedKey(group, key);
        Integer current = lastProcessed.get(cacheKey);
        return current != null && version <= current;
    }

    private void markProcessed(String group, String key, Integer version) {
        if (version == null) {
            return;
        }
        String cacheKey = buildProcessedKey(group, key);
        lastProcessed.put(cacheKey, version);
    }

    private String buildProcessedKey(String group, String key) {
        String g = group == null ? "" : group;
        String k = key == null ? "" : key;
        return g + ":" + k;
    }

    private void fallbackRefresh() {
        if (bindingManager == null) {
            return;
        }
        try {
            bindingManager.refreshAllHotUpdate(true);
        } catch (Exception ex) {
            log.debug("Fallback hot-update refresh failed.", ex);
        }
    }

    private long resolveCursor() {
        String key = resolveCursorKey();
        Object value = cacheTool.get(key);
        Long parsed = parseLong(value);
        return parsed == null ? 0L : Math.max(0L, parsed);
    }

    private void persistCursor(long id) {
        if (id <= 0) {
            return;
        }
        String key = resolveCursorKey();
        cacheTool.set(key, id, Duration.ofSeconds(resolveCursorTtlSeconds()));
    }

    private String resolveCursorKey() {
        String prefix = properties.getCursorKeyPrefix();
        String nodeId = nodeIdProvider == null ? "local" : nodeIdProvider.get();
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "config:change:cursor:";
        }
        return prefix + nodeId;
    }

    private int resolveCursorTtlSeconds() {
        int seconds = properties.getCursorTtlSeconds();
        return seconds <= 0 ? 86400 : seconds;
    }

    private Long parseLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveQueueKey() {
        String key = properties.getQueueKey();
        return key == null || key.trim().isEmpty() ? "config:change:queue" : key.trim();
    }

    private static final class LruMap<K, V> {
        private final int maxSize;
        private final LinkedHashMap<K, V> store;

        private LruMap(int maxSize) {
            this.maxSize = Math.max(1, maxSize);
            this.store = new LinkedHashMap<K, V>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > LruMap.this.maxSize;
                }
            };
        }

        private V get(K key) {
            synchronized (store) {
                return store.get(key);
            }
        }

        private void put(K key, V value) {
            synchronized (store) {
                store.put(key, value);
            }
        }
    }
}
