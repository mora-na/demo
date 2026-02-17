package com.example.demo.extension.executor;

import com.example.demo.extension.registry.DynamicApiMeta;
import com.example.demo.extension.support.DynamicApiRequest;
import lombok.Getter;

/**
 * 动态接口执行上下文。
 */
@Getter
public class DynamicApiContext {

    private final DynamicApiMeta meta;
    private final DynamicApiRequest request;
    private final long timeoutMs;

    public DynamicApiContext(DynamicApiMeta meta, DynamicApiRequest request, long timeoutMs) {
        this.meta = meta;
        this.request = request;
        this.timeoutMs = timeoutMs;
    }
}
