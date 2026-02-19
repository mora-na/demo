package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.registry.DynamicApiMeta;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * 动态接口执行上下文。
 */
@Getter
public class DynamicApiContext implements DynamicApiExecutionContext {

    private final DynamicApiMeta meta;
    private final DynamicApiRequest request;
    private final long timeoutMs;
    private final String traceId;
    private final String requestId;
    private final String tenantId;
    private final Map<String, Object> attributes;

    public DynamicApiContext(DynamicApiMeta meta,
                             DynamicApiRequest request,
                             long timeoutMs,
                             String traceId,
                             String requestId,
                             String tenantId,
                             Map<String, Object> attributes) {
        this.meta = meta;
        this.request = request;
        this.timeoutMs = timeoutMs;
        this.traceId = traceId;
        this.requestId = requestId;
        this.tenantId = tenantId;
        this.attributes = attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
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

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
