package com.example.demo.extension.metrics;

import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.dto.DynamicApiMetricItem;
import com.example.demo.extension.dto.DynamicApiMetricsSnapshot;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 动态接口指标采集。
 */
@Component
public class DynamicApiMetrics {

    private final DynamicApiProperties properties;
    private final MetricBucket global = new MetricBucket();
    private final ConcurrentHashMap<Long, MetricBucket> perApi = new ConcurrentHashMap<>();

    public DynamicApiMetrics(DynamicApiProperties properties) {
        this.properties = properties;
    }

    public void recordSubmit(DynamicApiMeta meta) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        MetricBucket bucket = perApi.computeIfAbsent(meta.getApi().getId(), id -> new MetricBucket());
        bucket.updateMeta(meta);
        bucket.total.increment();
        bucket.inflight.increment();
        bucket.lastUpdate.set(System.currentTimeMillis());
        global.total.increment();
        global.inflight.increment();
        global.lastUpdate.set(System.currentTimeMillis());
    }

    public void recordStart(DynamicApiMeta meta, long queueMs) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        MetricBucket bucket = perApi.computeIfAbsent(meta.getApi().getId(), id -> new MetricBucket());
        bucket.queueTimeMs.add(queueMs);
        bucket.lastUpdate.set(System.currentTimeMillis());
        global.queueTimeMs.add(queueMs);
        global.lastUpdate.set(System.currentTimeMillis());
    }

    public void recordComplete(DynamicApiMeta meta,
                               boolean success,
                               DynamicApiTerminationReason reason,
                               long execMs,
                               long totalMs) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        MetricBucket bucket = perApi.computeIfAbsent(meta.getApi().getId(), id -> new MetricBucket());
        bucket.updateMeta(meta);
        bucket.inflight.decrement();
        global.inflight.decrement();

        bucket.completed.increment();
        global.completed.increment();
        bucket.execTimeMs.add(execMs);
        bucket.totalTimeMs.add(totalMs);
        global.execTimeMs.add(execMs);
        global.totalTimeMs.add(totalMs);

        if (success) {
            bucket.success.increment();
            global.success.increment();
        } else {
            bucket.failure.increment();
            global.failure.increment();
            if (reason == DynamicApiTerminationReason.TIMEOUT) {
                bucket.timeout.increment();
                global.timeout.increment();
            } else if (reason == DynamicApiTerminationReason.CANCELLED) {
                bucket.cancelled.increment();
                global.cancelled.increment();
            }
        }
        bucket.lastUpdate.set(System.currentTimeMillis());
        global.lastUpdate.set(System.currentTimeMillis());
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
        MetricBucket bucket = perApi.computeIfAbsent(meta.getApi().getId(), id -> new MetricBucket());
        bucket.updateMeta(meta);
        if (rollbackInflight) {
            bucket.inflight.decrement();
            global.inflight.decrement();
        } else {
            bucket.total.increment();
            global.total.increment();
        }
        if (reason == DynamicApiTerminationReason.CIRCUIT_OPEN) {
            bucket.circuitOpen.increment();
            global.circuitOpen.increment();
        } else {
            bucket.rejected.increment();
            global.rejected.increment();
        }
        bucket.lastUpdate.set(System.currentTimeMillis());
        global.lastUpdate.set(System.currentTimeMillis());
    }

    public DynamicApiMetricsSnapshot snapshot() {
        if (!isEnabled()) {
            return new DynamicApiMetricsSnapshot(global.toSnapshot(null), Collections.emptyList());
        }
        int maxDetails = properties.getMetrics() == null ? 200 : Math.max(1, properties.getMetrics().getMaxDetails());
        List<DynamicApiMetricItem> items = new ArrayList<>();
        for (MetricBucket bucket : perApi.values()) {
            items.add(bucket.toSnapshot(null));
        }
        items.sort(Comparator.comparingLong(DynamicApiMetricItem::getTotal).reversed());
        if (items.size() > maxDetails) {
            items = new ArrayList<>(items.subList(0, maxDetails));
        }
        return new DynamicApiMetricsSnapshot(global.toSnapshot("global"), items);
    }

    private boolean isEnabled() {
        return properties != null && properties.getMetrics() != null && properties.getMetrics().isEnabled();
    }

    private static class MetricBucket {
        private final LongAdder total = new LongAdder();
        private final LongAdder completed = new LongAdder();
        private final LongAdder success = new LongAdder();
        private final LongAdder failure = new LongAdder();
        private final LongAdder timeout = new LongAdder();
        private final LongAdder cancelled = new LongAdder();
        private final LongAdder rejected = new LongAdder();
        private final LongAdder circuitOpen = new LongAdder();
        private final LongAdder inflight = new LongAdder();
        private final LongAdder queueTimeMs = new LongAdder();
        private final LongAdder execTimeMs = new LongAdder();
        private final LongAdder totalTimeMs = new LongAdder();
        private final AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
        private volatile Long apiId;
        private volatile String path;
        private volatile String method;
        private volatile String type;

        private void updateMeta(DynamicApiMeta meta) {
            if (meta == null || meta.getApi() == null) {
                return;
            }
            if (apiId == null && meta.getApi().getId() != null) {
                apiId = meta.getApi().getId();
            }
            if (path == null && meta.getApi().getPath() != null) {
                path = meta.getApi().getPath();
            }
            if (method == null && meta.getApi().getMethod() != null) {
                method = meta.getApi().getMethod();
            }
            if (type == null && meta.getType() != null) {
                type = meta.getType();
            }
        }

        private DynamicApiMetricItem toSnapshot(String name) {
            DynamicApiMetricItem item = new DynamicApiMetricItem();
            item.setName(name);
            item.setApiId(apiId);
            item.setPath(path);
            item.setMethod(method);
            item.setType(type);
            long totalValue = total.sum();
            long completedValue = completed.sum();
            item.setTotal(totalValue);
            item.setSuccess(success.sum());
            item.setFailure(failure.sum());
            item.setTimeout(timeout.sum());
            item.setCancelled(cancelled.sum());
            item.setRejected(rejected.sum());
            item.setCircuitOpen(circuitOpen.sum());
            item.setInflight(inflight.sum());
            item.setAvgQueueMs(completedValue == 0 ? 0d : queueTimeMs.sum() * 1.0d / completedValue);
            item.setAvgExecuteMs(completedValue == 0 ? 0d : execTimeMs.sum() * 1.0d / completedValue);
            item.setAvgTotalMs(completedValue == 0 ? 0d : totalTimeMs.sum() * 1.0d / completedValue);
            item.setLastUpdateTime(lastUpdate.get());
            return item;
        }
    }
}
