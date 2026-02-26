package com.example.demo.extension.executor;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.registry.DynamicApiMeta;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * 简易动态接口熔断器。
 */
@Component
public class DynamicApiCircuitBreaker {

    private static final String STATE_KEY_PREFIX = "dynamic.api:circuit:";

    private final DynamicApiProperties properties;
    private final CacheTool cacheTool;

    public DynamicApiCircuitBreaker(DynamicApiProperties properties, CacheTool cacheTool) {
        this.properties = properties;
        this.cacheTool = cacheTool;
    }

    public boolean allow(DynamicApiMeta meta) {
        if (!isEnabled() || meta == null || meta.getApi() == null || meta.getApi().getId() == null) {
            return true;
        }
        long apiId = meta.getApi().getId();
        long now = System.currentTimeMillis();
        Boolean result = withStateLock(apiId, () -> {
            CircuitState state = loadState(apiId);
            boolean allow = true;
            if (state.openUntil > 0 && now < state.openUntil) {
                allow = false;
            } else {
                if (state.openUntil > 0 && now >= state.openUntil) {
                    state.reset(now);
                }
                if (now - state.windowStart >= getWindowMs()) {
                    state.reset(now);
                }
            }
            saveState(apiId, state);
            return allow;
        });
        return result == null ? true : result;
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
        withStateLock(apiId, () -> {
            CircuitState state = loadState(apiId);
            if (state.openUntil > 0 && now < state.openUntil) {
                return null;
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
            if (total >= getMinimumCalls()) {
                double failureRate = total == 0 ? 0d : (state.failure * 1.0d / total);
                if (failureRate >= getFailureRate()) {
                    state.openUntil = now + getOpenDurationMs();
                }
            }
            saveState(apiId, state);
            return null;
        });
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

    private int resolveExpireSeconds() {
        if (properties == null || properties.getCircuitBreaker() == null) {
            return 1800;
        }
        return Math.max(60, properties.getCircuitBreaker().getExpireAfterAccessSeconds());
    }

    private String buildStateKey(long apiId) {
        return STATE_KEY_PREFIX + apiId;
    }

    private String buildLockKey(long apiId) {
        return buildStateKey(apiId) + ":lock";
    }

    private <T> T withStateLock(long apiId, Supplier<T> action) {
        if (action == null) {
            return null;
        }
        String lockKey = buildLockKey(apiId);
        String token = cacheTool.tryLock(lockKey, Duration.ofSeconds(2));
        if (token == null) {
            return null;
        }
        try {
            return action.get();
        } finally {
            cacheTool.releaseLock(lockKey, token);
        }
    }

    private CircuitState loadState(long apiId) {
        Object value = cacheTool.get(buildStateKey(apiId));
        if (value instanceof CircuitState) {
            return (CircuitState) value;
        }
        return new CircuitState();
    }

    private void saveState(long apiId, CircuitState state) {
        cacheTool.set(buildStateKey(apiId), state, Duration.ofSeconds(resolveExpireSeconds()));
    }

    @Data
    private static class CircuitState implements Serializable {
        private static final long serialVersionUID = 1L;
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
