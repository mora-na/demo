package com.example.demo.extension.support;

import lombok.Builder;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * 动态接口请求上下文。
 */
@Getter
@Builder
public class DynamicApiRequest {

    private final HttpServletRequest rawRequest;
    private final String path;
    private final String method;
    private final Map<String, Object> params;
    private final Map<String, String> pathVariables;
    private final Map<String, String> headers;
    private final Object body;
    private final String rawBody;

    public Map<String, Object> getParams() {
        return params == null ? Collections.emptyMap() : params;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables == null ? Collections.emptyMap() : pathVariables;
    }

    public Map<String, String> getHeaders() {
        return headers == null ? Collections.emptyMap() : headers;
    }
}
