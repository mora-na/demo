package com.example.demo.common.web.filter;

import com.example.demo.common.config.CommonConstants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器，生成并绑定请求链路标识到 MDC。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String HEADER_CLIENT_REQUEST_ID = "X-Client-Request-Id";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String HEADER_UPSTREAM_TRACE_ID = "X-Upstream-Trace-Id";
    private static final String HEADER_CF_RAY = "CF-Ray";
    private static final String HEADER_X_CF_RAY = "X-Cf-Ray";
    private static final String ATTR_CLIENT_REQUEST_ID = "clientRequestId";
    private static final String ATTR_TRACE_ID = "traceId";

    private final CommonConstants systemConstants;

    public TraceIdFilter(CommonConstants systemConstants) {
        this.systemConstants = systemConstants;
    }

    /**
     * 为当前请求绑定 traceId 并在请求结束后清理。
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws IOException, ServletException {
        String mdcKey = systemConstants.getTrace().getMdcKey();
        String headerClientRequestId = firstNonBlank(request.getHeader(HEADER_CLIENT_REQUEST_ID));
        String headerRequestId = firstNonBlank(request.getHeader(HEADER_REQUEST_ID));
        String headerTraceId = firstNonBlank(request.getHeader(HEADER_TRACE_ID));
        String headerCfRay = firstNonBlank(request.getHeader(HEADER_X_CF_RAY), request.getHeader(HEADER_CF_RAY));
        String clientRequestId = resolveClientRequestId(request);
        String traceId = resolveTraceId(request);
        String cfRay = headerCfRay;
        String traceSource = headerTraceId != null ? "header-trace-id" : "generated";
        org.slf4j.LoggerFactory.getLogger(TraceIdFilter.class).info(
                "http.trace.bind method={} uri={} headerClientRequestId={} headerRequestId={} headerTraceId={} headerCfRay={} resolvedClientRequestId={} resolvedTraceId={} traceSource={}",
                request.getMethod(), request.getRequestURI(), headerClientRequestId, headerRequestId, headerTraceId, headerCfRay,
                clientRequestId, traceId, traceSource
        );
        try {
            MDC.put(mdcKey, traceId);
            request.setAttribute(ATTR_CLIENT_REQUEST_ID, clientRequestId);
            request.setAttribute(ATTR_TRACE_ID, traceId);
            response.setHeader(HEADER_CLIENT_REQUEST_ID, clientRequestId);
            response.setHeader(HEADER_TRACE_ID, traceId);
            response.setHeader(HEADER_UPSTREAM_TRACE_ID, traceId);
            if (cfRay != null) {
                response.setHeader(HEADER_X_CF_RAY, cfRay);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(mdcKey);
        }
    }

    private String resolveClientRequestId(HttpServletRequest request) {
        String candidate = firstNonBlank(
                request.getHeader(HEADER_CLIENT_REQUEST_ID),
                request.getHeader(HEADER_REQUEST_ID)
        );
        return candidate != null ? candidate : UUID.randomUUID().toString();
    }

    private String resolveTraceId(HttpServletRequest request) {
        String candidate = firstNonBlank(
                request.getHeader(HEADER_TRACE_ID),
                (String) request.getAttribute(ATTR_TRACE_ID)
        );
        return candidate != null ? candidate : UUID.randomUUID().toString();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }
}
