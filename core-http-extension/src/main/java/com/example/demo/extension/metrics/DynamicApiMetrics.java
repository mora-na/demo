package com.example.demo.extension.metrics;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.dto.DynamicApiMetricItem;
import com.example.demo.extension.dto.DynamicApiMetricsSnapshot;
import com.example.demo.extension.manager.DynamicApiService;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 动态接口指标采集（跨节点共享）。
 */
@Component
public class DynamicApiMetrics {

    private static final String GLOBAL_KEY = "dynamic:metrics:global";
    private static final String API_KEY_PREFIX = "dynamic:metrics:api:";

    private static final String FIELD_TOTAL = "total";
    private static final String FIELD_COMPLETED = "completed";
    private static final String FIELD_SUCCESS = "success";
    private static final String FIELD_FAILURE = "failure";
    private static final String FIELD_TIMEOUT = "timeout";
    private static final String FIELD_CANCELLED = "cancelled";
    private static final String FIELD_REJECTED = "rejected";
    private static final String FIELD_CIRCUIT_OPEN = "circuitOpen";
    private static final String FIELD_INFLIGHT = "inflight";
    private static final String FIELD_QUEUE_TIME = "queueTimeMs";
    private static final String FIELD_EXEC_TIME = "execTimeMs";
    private static final String FIELD_TOTAL_TIME = "totalTimeMs";
    private static final String FIELD_LAST_UPDATE = "lastUpdate";

    private final DynamicApiProperties properties;
    private final CacheTool cacheTool;
    private final DynamicApiService dynamicApiService;

    public DynamicApiMetrics(DynamicApiProperties properties,
                             CacheTool cacheTool,
                             DynamicApiService dynamicApiService) {
        this.properties = properties;
        this.cacheTool = cacheTool;
        this.dynamicApiService = dynamicApiService;
    }

    public void recordSubmit(DynamicApiMeta meta) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        if (shouldRecordDetail()) {
            String apiKey = buildApiKey(meta.getApi().getId());
            incr(apiKey, FIELD_TOTAL, 1);
            incr(apiKey, FIELD_INFLIGHT, 1);
            touch(apiKey);
        }

        incr(GLOBAL_KEY, FIELD_TOTAL, 1);
        incr(GLOBAL_KEY, FIELD_INFLIGHT, 1);
        touch(GLOBAL_KEY);
    }

    public void recordStart(DynamicApiMeta meta, long queueMs) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        if (shouldRecordDetail()) {
            String apiKey = buildApiKey(meta.getApi().getId());
            incr(apiKey, FIELD_QUEUE_TIME, queueMs);
            touch(apiKey);
        }

        incr(GLOBAL_KEY, FIELD_QUEUE_TIME, queueMs);
        touch(GLOBAL_KEY);
    }

    public void recordComplete(DynamicApiMeta meta,
                               boolean success,
                               DynamicApiTerminationReason reason,
                               long execMs,
                               long totalMs) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        boolean recordDetail = shouldRecordDetail();
        String apiKey = recordDetail ? buildApiKey(meta.getApi().getId()) : null;
        if (recordDetail) {
            incr(apiKey, FIELD_INFLIGHT, -1);
            incr(apiKey, FIELD_COMPLETED, 1);
            incr(apiKey, FIELD_EXEC_TIME, execMs);
            incr(apiKey, FIELD_TOTAL_TIME, totalMs);
            touch(apiKey);
        }

        incr(GLOBAL_KEY, FIELD_INFLIGHT, -1);
        incr(GLOBAL_KEY, FIELD_COMPLETED, 1);
        incr(GLOBAL_KEY, FIELD_EXEC_TIME, execMs);
        incr(GLOBAL_KEY, FIELD_TOTAL_TIME, totalMs);
        touch(GLOBAL_KEY);

        if (success) {
            incr(GLOBAL_KEY, FIELD_SUCCESS, 1);
            if (recordDetail) {
                incr(apiKey, FIELD_SUCCESS, 1);
            }
        } else {
            incr(GLOBAL_KEY, FIELD_FAILURE, 1);
            if (recordDetail) {
                incr(apiKey, FIELD_FAILURE, 1);
            }
            if (reason == DynamicApiTerminationReason.TIMEOUT) {
                incr(GLOBAL_KEY, FIELD_TIMEOUT, 1);
                if (recordDetail) {
                    incr(apiKey, FIELD_TIMEOUT, 1);
                }
            } else if (reason == DynamicApiTerminationReason.CANCELLED) {
                incr(GLOBAL_KEY, FIELD_CANCELLED, 1);
                if (recordDetail) {
                    incr(apiKey, FIELD_CANCELLED, 1);
                }
            }
        }
        if (recordDetail) {
            touch(apiKey);
        }
        touch(GLOBAL_KEY);
    }

    public void recordReject(DynamicApiMeta meta, DynamicApiTerminationReason reason) {
        recordRejectInternal(meta, reason, false);
    }

    public void recordRejectAfterSubmit(DynamicApiMeta meta, DynamicApiTerminationReason reason) {
        recordRejectInternal(meta, reason, true);
    }

    private void recordRejectInternal(DynamicApiMeta meta, DynamicApiTerminationReason reason, boolean rollbackInflight) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        boolean recordDetail = shouldRecordDetail();
        String apiKey = recordDetail ? buildApiKey(meta.getApi().getId()) : null;
        if (rollbackInflight) {
            incr(GLOBAL_KEY, FIELD_INFLIGHT, -1);
            if (recordDetail) {
                incr(apiKey, FIELD_INFLIGHT, -1);
            }
        } else {
            incr(GLOBAL_KEY, FIELD_TOTAL, 1);
            if (recordDetail) {
                incr(apiKey, FIELD_TOTAL, 1);
            }
        }
        if (reason == DynamicApiTerminationReason.CIRCUIT_OPEN) {
            incr(GLOBAL_KEY, FIELD_CIRCUIT_OPEN, 1);
            if (recordDetail) {
                incr(apiKey, FIELD_CIRCUIT_OPEN, 1);
            }
        } else {
            incr(GLOBAL_KEY, FIELD_REJECTED, 1);
            if (recordDetail) {
                incr(apiKey, FIELD_REJECTED, 1);
            }
        }
        if (recordDetail) {
            touch(apiKey);
        }
        touch(GLOBAL_KEY);
    }

    public DynamicApiMetricsSnapshot snapshot() {
        if (!isEnabled()) {
            return new DynamicApiMetricsSnapshot(readMetrics(GLOBAL_KEY, null, null), Collections.emptyList());
        }
        DynamicApiMetricItem global = readMetrics(GLOBAL_KEY, "global", null);
        if (!shouldRecordDetail()) {
            return new DynamicApiMetricsSnapshot(global, Collections.emptyList());
        }
        List<DynamicApi> enabled = dynamicApiService == null ? Collections.emptyList() : dynamicApiService.listEnabled();
        if (enabled == null || enabled.isEmpty()) {
            return new DynamicApiMetricsSnapshot(global, Collections.emptyList());
        }
        long now = System.currentTimeMillis();
        long expireMillis = resolveExpireSeconds() * 1000L;
        List<DynamicApiMetricItem> items = new ArrayList<>();
        for (DynamicApi api : enabled) {
            if (api == null || api.getId() == null) {
                continue;
            }
            String apiKey = buildApiKey(api.getId());
            DynamicApiMetricItem item = readMetrics(apiKey, null, api);
            if (isStale(item, now, expireMillis)) {
                cacheTool.delete(apiKey);
                continue;
            }
            if (item.getTotal() > 0 || item.getInflight() > 0) {
                items.add(item);
            }
        }
        items.sort(Comparator.comparingLong(DynamicApiMetricItem::getTotal).reversed());
        int maxDetails = properties.getMetrics() == null ? 200 : Math.max(1, properties.getMetrics().getMaxDetails());
        if (items.size() > maxDetails) {
            items = new ArrayList<>(items.subList(0, maxDetails));
        }
        return new DynamicApiMetricsSnapshot(global, items);
    }

    private boolean isEnabled() {
        return properties != null && properties.getMetrics() != null && properties.getMetrics().isEnabled();
    }

    private boolean shouldRecordDetail() {
        if (properties == null || properties.getMetrics() == null) {
            return true;
        }
        DynamicApiProperties.Metrics metrics = properties.getMetrics();
        if (!metrics.isAutoDegradeEnabled()) {
            return true;
        }
        double ratio = resolveDegradeRatio(metrics.getDegradeRatio());
        if (ratio <= 0) {
            return true;
        }
        int max = Math.max(1, metrics.getMaxEntries());
        List<DynamicApi> enabled = dynamicApiService == null ? Collections.emptyList() : dynamicApiService.listEnabled();
        int size = enabled == null ? 0 : enabled.size();
        return size < Math.ceil(max * ratio);
    }

    private double resolveDegradeRatio(double ratio) {
        if (ratio <= 0) {
            return 0d;
        }
        return Math.min(1d, ratio);
    }

    private String buildApiKey(Long apiId) {
        return API_KEY_PREFIX + apiId;
    }

    private void incr(String key, String field, long delta) {
        if (key == null || field == null) {
            return;
        }
        Long next = cacheTool.hincrBy(key, field, delta);
        if (next != null && FIELD_INFLIGHT.equals(field) && next < 0) {
            cacheTool.hset(key, field, 0L);
        }
        expire(key);
    }

    private void touch(String key) {
        cacheTool.hset(key, FIELD_LAST_UPDATE, System.currentTimeMillis());
        expire(key);
    }

    private void expire(String key) {
        int seconds = resolveExpireSeconds();
        if (seconds > 0) {
            cacheTool.expire(key, Duration.ofSeconds(seconds));
        }
    }

    private int resolveExpireSeconds() {
        if (properties == null || properties.getMetrics() == null) {
            return 900;
        }
        return Math.max(60, properties.getMetrics().getExpireAfterAccessSeconds());
    }

    private boolean isStale(DynamicApiMetricItem item, long now, long expireMillis) {
        if (item == null || expireMillis <= 0) {
            return false;
        }
        long last = item.getLastUpdateTime();
        return last > 0 && now - last > expireMillis;
    }

    private DynamicApiMetricItem readMetrics(String key, String name, DynamicApi api) {
        DynamicApiMetricItem item = new DynamicApiMetricItem();
        item.setName(name);
        if (api != null) {
            item.setApiId(api.getId());
            item.setPath(api.getPath());
            item.setMethod(api.getMethod());
            item.setType(api.getType());
        }
        List<Object> values = cacheTool.hmget(key,
                FIELD_TOTAL,
                FIELD_COMPLETED,
                FIELD_SUCCESS,
                FIELD_FAILURE,
                FIELD_TIMEOUT,
                FIELD_CANCELLED,
                FIELD_REJECTED,
                FIELD_CIRCUIT_OPEN,
                FIELD_INFLIGHT,
                FIELD_QUEUE_TIME,
                FIELD_EXEC_TIME,
                FIELD_TOTAL_TIME,
                FIELD_LAST_UPDATE);
        long total = readLong(values, 0);
        long completed = readLong(values, 1);
        item.setTotal(total);
        item.setSuccess(readLong(values, 2));
        item.setFailure(readLong(values, 3));
        item.setTimeout(readLong(values, 4));
        item.setCancelled(readLong(values, 5));
        item.setRejected(readLong(values, 6));
        item.setCircuitOpen(readLong(values, 7));
        item.setInflight(readLong(values, 8));
        item.setAvgQueueMs(completed == 0 ? 0d : readLong(values, 9) * 1.0d / completed);
        item.setAvgExecuteMs(completed == 0 ? 0d : readLong(values, 10) * 1.0d / completed);
        item.setAvgTotalMs(completed == 0 ? 0d : readLong(values, 11) * 1.0d / completed);
        item.setLastUpdateTime(readLong(values, 12));
        return item;
    }

    private long readLong(List<Object> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return 0L;
        }
        Object value = values.get(index);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
