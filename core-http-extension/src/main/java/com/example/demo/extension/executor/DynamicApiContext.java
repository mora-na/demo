package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.registry.DynamicApiMeta;
import lombok.Getter;

/**
 * 动态接口执行上下文。
 */
@Getter
public class DynamicApiContext implements DynamicApiExecutionContext {

    private final DynamicApiMeta meta;
    private final DynamicApiRequest request;
    private final long timeoutMs;

    public DynamicApiContext(DynamicApiMeta meta, DynamicApiRequest request, long timeoutMs) {
        this.meta = meta;
        this.request = request;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public String getType() {
        return meta == null ? null : meta.getType();
    }

    @Override
    public Object getConfig() {
        return meta == null ? null : meta.getConfig();
    }

    @Override
    public Long getApiId() {
        return meta == null || meta.getApi() == null ? null : meta.getApi().getId();
    }

    @Override
    public String getPath() {
        return meta == null || meta.getApi() == null ? null : meta.getApi().getPath();
    }

    @Override
    public String getMethod() {
        return meta == null || meta.getApi() == null ? null : meta.getApi().getMethod();
    }
}
