package com.example.demo.common.web.filter;

import com.example.demo.common.web.support.ClientIpResolver;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一打印 HTTP 请求入口和完成日志，便于跨前端/Worker/后端追踪。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/28
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RequestTraceLoggingFilter extends OncePerRequestFilter {

    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_CF_RAY = "CF-Ray";
    private static final String HEADER_X_CF_RAY = "X-Cf-Ray";
    private static final String HEADER_X_FROM_WORKER = "X-From-Worker";
    private static final String HEADER_ORIGIN = "Origin";

    private final ClientIpResolver clientIpResolver;

    public RequestTraceLoggingFilter(ClientIpResolver clientIpResolver) {
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        String requestId = firstNonBlank(request.getHeader(HEADER_REQUEST_ID), (String) request.getAttribute(HEADER_REQUEST_ID));
        String cfRay = firstNonBlank(request.getHeader(HEADER_X_CF_RAY), request.getHeader(HEADER_CF_RAY));
        String requestUri = buildRequestUri(request);
        String clientIp = clientIpResolver.resolve(request);
        String origin = trimToNull(request.getHeader(HEADER_ORIGIN));
        boolean fromWorker = "true".equalsIgnoreCase(request.getHeader(HEADER_X_FROM_WORKER));

        log.info("http.request.start requestId={} cfRay={} method={} uri={} clientIp={} origin={} fromWorker={}",
                requestId, cfRay, request.getMethod(), requestUri, clientIp, origin, fromWorker);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.error("http.request.error requestId={} cfRay={} method={} uri={} status={} durationMs={} error={}",
                    requestId, cfRay, request.getMethod(), requestUri, response.getStatus(), durationMs, ex.getMessage(), ex);
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("http.request.complete requestId={} cfRay={} method={} uri={} status={} durationMs={}",
                    requestId, cfRay, request.getMethod(), requestUri, response.getStatus(), durationMs);
        }
    }

    private String buildRequestUri(HttpServletRequest request) {
        String query = trimToNull(request.getQueryString());
        return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String candidate = trimToNull(value);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
