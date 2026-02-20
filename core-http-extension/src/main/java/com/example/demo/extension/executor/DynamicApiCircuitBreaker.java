package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 简易动态接口熔断器。
 */
@Component
public class DynamicApiCircuitBreaker {

    private final DynamicApiProperties properties;
    private final Cache<Long, CircuitState> states;

    public DynamicApiCircuitBreaker(DynamicApiProperties properties) {
        this.properties = properties;
        this.states = buildStateCache();
    }

    public boolean allow(DynamicApiMeta meta) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return true;
        }
        long apiId = meta.getApi().getId();
        long now = System.currentTimeMillis();
        CircuitState state = states.get(apiId, id -> new CircuitState());
        synchronized (state) {
            if (state.openUntil > 0 && now < state.openUntil) {
                return false;
            }
            if (state.openUntil > 0 && now >= state.openUntil) {
                state.reset(now);
            }
            if (now - state.windowStart >= getWindowMs()) {
                state.reset(now);
            }
            return true;
        }
    }

    public void record(DynamicApiMeta meta, boolean success, DynamicApiTerminationReason reason) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return;
        }
        if (reason == DynamicApiTerminationReason.REJECTED || reason == DynamicApiTerminationReason.CIRCUIT_OPEN) {
            return;
        }
        long apiId = meta.getApi().getId();
        long now = System.currentTimeMillis();
        CircuitState state = states.get(apiId, id -> new CircuitState());
        synchronized (state) {
            if (state.openUntil > 0 && now < state.openUntil) {
                return;
            }
            if (now - state.windowStart >= getWindowMs()) {
                state.reset(now);
            }
            if (success) {
                state.success++;
            } else {
                state.failure++;
            }
            int total = state.success + state.failure;
            if (total < getMinimumCalls()) {
                return;
            }
            double failureRate = total == 0 ? 0d : (state.failure * 1.0d / total);
            if (failureRate >= getFailureRate()) {
                state.openUntil = now + getOpenDurationMs();
            }
        }
    }

    private boolean isEnabled() {
        return properties != null && properties.getCircuitBreaker() != null && properties.getCircuitBreaker().isEnabled();
    }

    private long getWindowMs() {
        int seconds = properties.getCircuitBreaker().getWindowSeconds();
        return Math.max(1, seconds) * 1000L;
    }

    private int getMinimumCalls() {
        return Math.max(1, properties.getCircuitBreaker().getMinimumCalls());
    }

    private double getFailureRate() {
        double rate = properties.getCircuitBreaker().getFailureRate();
        if (rate < 0) {
            return 0;
        }
        return Math.min(1d, rate);
    }

    private long getOpenDurationMs() {
        return Math.max(1000L, properties.getCircuitBreaker().getOpenDurationMs());
    }

    private Cache<Long, CircuitState> buildStateCache() {
        int maxEntries = resolveMaxEntries();
        int expireSeconds = resolveExpireSeconds();
        return Caffeine.newBuilder()
                .maximumSize(maxEntries)
                .expireAfterAccess(Duration.ofSeconds(expireSeconds))
                .build();
    }

    private int resolveMaxEntries() {
        if (properties == null || properties.getCircuitBreaker() == null) {
            return 10000;
        }
        return Math.max(1, properties.getCircuitBreaker().getMaxEntries());
    }

    private int resolveExpireSeconds() {
        if (properties == null || properties.getCircuitBreaker() == null) {
            return 1800;
        }
        return Math.max(60, properties.getCircuitBreaker().getExpireAfterAccessSeconds());
    }

    private static class CircuitState {
        private long windowStart = System.currentTimeMillis();
        private long openUntil = 0;
        private int success = 0;
        private int failure = 0;

        private void reset(long now) {
            windowStart = now;
            openUntil = 0;
            success = 0;
            failure = 0;
        }
    }
}
