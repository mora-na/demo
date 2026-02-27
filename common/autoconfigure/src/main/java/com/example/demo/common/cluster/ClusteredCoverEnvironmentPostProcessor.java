package com.example.demo.common.cluster;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cluster-aware configuration override.
 *
 * <p>When {@code server.is-clustered=true}, all key/value pairs defined under
 * {@code server.clustered-cover-config} will be injected with highest precedence,
 * forcing multi-node-safe defaults without hardcoding the list in code.</p>
 *
 * <p>When {@code server.is-clustered=false}, no override is applied and all user
 * configurations remain untouched.</p>
 */
public class ClusteredCoverEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "serverClusteredCoverOverrides";
    private static final String CLUSTERED_FLAG = "server.is-clustered";
    private static final String COVER_PREFIX = "server.clustered-cover-config";
    private final Log log;

    public ClusteredCoverEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Boolean clustered = environment.getProperty(CLUSTERED_FLAG, Boolean.class);
        if (clustered == null || !clustered) {
            log.info("EnvironmentPostProcessor loaded: " + PROPERTY_SOURCE_NAME
                    + ", skipped (" + CLUSTERED_FLAG + "=false)");
            return;
        }
        Map<String, Object> raw = Binder.get(environment)
                .bind(COVER_PREFIX, Bindable.mapOf(String.class, Object.class))
                .orElseGet(LinkedHashMap::new);
        if (raw.isEmpty()) {
            log.info("EnvironmentPostProcessor loaded: " + PROPERTY_SOURCE_NAME
                    + ", no overrides under " + COVER_PREFIX);
            return;
        }
        Map<String, Object> overrides = new LinkedHashMap<>();
        flatten("", raw, overrides);
        if (!overrides.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
            log.info("EnvironmentPostProcessor loaded: " + PROPERTY_SOURCE_NAME
                    + ", applied " + overrides.size() + " override(s) from " + COVER_PREFIX);
        }
    }

    private void flatten(String prefix, Map<String, Object> source, Map<String, Object> target) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (entry == null) {
                continue;
            }
            String key = entry.getKey();
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            String trimmed = key.trim();
            String fullKey = prefix == null || prefix.isEmpty() ? trimmed : prefix + "." + trimmed;
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) value;
                flatten(fullKey, child, target);
                continue;
            }
            if (value != null) {
                target.put(fullKey, value);
            }
        }
    }

    @Override
    public int getOrder() {
        // Run after ConfigDataEnvironmentPostProcessor and other custom processors.
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
