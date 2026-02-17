package com.example.demo.extension.executor;

import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApiType;
import com.example.demo.extension.model.HttpForwardConfig;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * HTTP 转发策略。
 */
@Component
public class HttpForwardStrategy implements ExecuteStrategy {

    private final DynamicApiConstants constants;

    public HttpForwardStrategy(DynamicApiConstants constants) {
        this.constants = constants;
    }

    @Override
    public DynamicApiType type() {
        return DynamicApiType.HTTP;
    }

    @Override
    public DynamicApiExecuteResult execute(DynamicApiContext context) {
        DynamicApiMeta meta = context.getMeta();
        Object configObj = meta.getConfig();
        if (!(configObj instanceof HttpForwardConfig)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid());
        }
        HttpForwardConfig config = (HttpForwardConfig) configObj;
        if (StringUtils.isBlank(config.getUrl())) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid());
        }
        HttpServletRequest request = context.getRequest().getRawRequest();
        String method = resolveMethod(config, request);
        HttpMethod httpMethod = HttpMethod.resolve(method);
        if (httpMethod == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getMethodInvalid());
        }
        String targetUrl = resolveUrl(config, request, context.getRequest().getPathVariables());
        HttpHeaders headers = buildHeaders(config, request);
        String body = context.getRequest().getRawBody();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            RestTemplate restTemplate = HttpClientFactory.create((int) context.getTimeoutMs());
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, httpMethod, entity, String.class);
            return DynamicApiExecuteResult.success(response.getBody());
        } catch (Exception ex) {
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed());
        }
    }

    private String resolveMethod(HttpForwardConfig config, HttpServletRequest request) {
        if (StringUtils.isNotBlank(config.getMethod())) {
            return config.getMethod().trim().toUpperCase(Locale.ROOT);
        }
        return request == null ? "GET" : request.getMethod();
    }

    private String resolveUrl(HttpForwardConfig config, HttpServletRequest request, java.util.Map<String, String> pathVariables) {
        String url = config.getUrl().trim();
        String expanded = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(pathVariables == null ? java.util.Collections.emptyMap() : pathVariables)
                .toUriString();
        if (config.isPassQuery() && request != null && request.getQueryString() != null) {
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) {
                String separator = expanded.contains("?") ? "&" : "?";
                return expanded + separator + query;
            }
        }
        return expanded;
    }

    private HttpHeaders buildHeaders(HttpForwardConfig config, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (request != null && config.isPassHeaders()) {
            Enumeration<String> names = request.getHeaderNames();
            if (names != null) {
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    if (name == null) {
                        continue;
                    }
                    if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                        continue;
                    }
                    headers.add(name, request.getHeader(name));
                }
            }
        }
        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            for (java.util.Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    headers.set(entry.getKey(), entry.getValue());
                }
            }
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE) && request != null && request.getContentType() != null) {
            headers.set(HttpHeaders.CONTENT_TYPE, request.getContentType());
        }
        return headers;
    }

    private static class HttpClientFactory {
        static RestTemplate create(int timeoutMs) {
            org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            int timeout = timeoutMs <= 0 ? 3000 : timeoutMs;
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            return new RestTemplate(factory);
        }
    }
}
