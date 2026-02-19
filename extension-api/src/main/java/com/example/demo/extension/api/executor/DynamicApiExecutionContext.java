package com.example.demo.extension.api.executor;

import com.example.demo.extension.api.request.DynamicApiRequest;

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
}
