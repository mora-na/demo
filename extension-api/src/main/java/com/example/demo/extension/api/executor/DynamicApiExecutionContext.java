package com.example.demo.extension.api.executor;

import com.example.demo.extension.api.request.DynamicApiRequest;

import java.util.Collections;
import java.util.Map;

/**
 * 动态接口执行上下文（对外 SPI）。
 */
public interface DynamicApiExecutionContext {

    DynamicApiRequest getRequest();

    long getTimeoutMs();

    String getType();

    Object getConfig();

    Long getApiId();

    String getPath();

    String getMethod();

    default String getTraceId() {
        return null;
    }

    default String getRequestId() {
        return null;
    }

    default String getTenantId() {
        return null;
    }

    default Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }
}
