package com.example.demo.extension.metrics;

import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.dto.DynamicApiMetricsSnapshot;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DynamicApiMetricsTest {

    @Test
    void recordAndSnapshot() {
        DynamicApiProperties properties = new DynamicApiProperties();
        DynamicApiProperties.Metrics metricsConfig = new DynamicApiProperties.Metrics();
        metricsConfig.setEnabled(true);
        metricsConfig.setMaxDetails(10);
        properties.setMetrics(metricsConfig);

        DynamicApiMetrics metrics = new DynamicApiMetrics(properties);
        DynamicApiMeta meta = buildMeta(1L, "/ext/orders", "SQL");

        metrics.recordSubmit(meta);
        metrics.recordStart(meta, 5);
        metrics.recordComplete(meta, true, null, 10, 20);

        DynamicApiMetricsSnapshot snapshot = metrics.snapshot();
        assertNotNull(snapshot);
        assertNotNull(snapshot.getGlobal());
        assertEquals(1, snapshot.getGlobal().getTotal());
        assertEquals(1, snapshot.getGlobal().getSuccess());
        assertEquals(1, snapshot.getItems().size());
        assertEquals(1, snapshot.getItems().get(0).getTotal());
    }

    @Test
    void recordReject() {
        DynamicApiProperties properties = new DynamicApiProperties();
        DynamicApiProperties.Metrics metricsConfig = new DynamicApiProperties.Metrics();
        metricsConfig.setEnabled(true);
        properties.setMetrics(metricsConfig);
        DynamicApiMetrics metrics = new DynamicApiMetrics(properties);

        DynamicApiMeta meta = buildMeta(2L, "/ext/reject", "HTTP");
        metrics.recordReject(meta, DynamicApiTerminationReason.REJECTED);

        DynamicApiMetricsSnapshot snapshot = metrics.snapshot();
        assertEquals(1, snapshot.getItems().get(0).getRejected());
    }

    private DynamicApiMeta buildMeta(Long id, String path, String type) {
        DynamicApi api = new DynamicApi();
        api.setId(id);
        api.setPath(path);
        api.setMethod("GET");
        api.setType(type);
        return new DynamicApiMeta(api, type, DynamicApiAuthMode.INHERIT, null, null);
    }
}
