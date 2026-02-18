package com.example.demo.extension.executor;

import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApiTypeCodes;
import com.example.demo.extension.model.HttpForwardConfig;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.example.demo.extension.support.DynamicApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 转发策略。
 */
@Slf4j
@Component
public class HttpForwardStrategy implements ExecuteStrategy {

    private final DynamicApiConstants constants;

    public HttpForwardStrategy(DynamicApiConstants constants) {
        this.constants = constants;
    }

    @Override
    public String type() {
        return DynamicApiTypeCodes.HTTP;
    }

    @Override
    public String displayName() {
        return "HTTP";
    }

    @Override
    public Object parseConfig(String configJson, ObjectMapper objectMapper) throws Exception {
        if (StringUtils.isBlank(configJson)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        HttpForwardConfig config = objectMapper.readValue(configJson, HttpForwardConfig.class);
        if (config == null || StringUtils.isBlank(config.getUrl())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid());
        }
        return config;
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
        DynamicApiRequest apiRequest = context.getRequest();
        String method = resolveMethod(config, apiRequest);
        HttpMethod httpMethod = HttpMethod.resolve(method);
        if (httpMethod == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getMethodInvalid());
        }
        String targetUrl = resolveUrl(config, apiRequest, context.getRequest().getPathVariables());
        HttpHeaders headers = buildHeaders(config, apiRequest);
        String body = apiRequest == null ? null : apiRequest.getRawBody();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            RestTemplate restTemplate = HttpClientFactory.create((int) context.getTimeoutMs());
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, httpMethod, entity, String.class);
            return DynamicApiExecuteResult.success(response.getBody());
        } catch (Exception ex) {
            String traceId = MDC.get("traceId");
            log.error("Dynamic api http execute failed: apiId={}, path={}, method={}, targetUrl={}, traceId={}",
                    context.getMeta().getApi().getId(),
                    context.getMeta().getApi().getPath(),
                    context.getMeta().getApi().getMethod(),
                    targetUrl,
                    traceId,
                    ex);
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed());
        }
    }

    private String resolveMethod(HttpForwardConfig config, DynamicApiRequest request) {
        if (StringUtils.isNotBlank(config.getMethod())) {
            return config.getMethod().trim().toUpperCase(Locale.ROOT);
        }
        return request == null ? "GET" : request.getMethod();
    }

    private String resolveUrl(HttpForwardConfig config, DynamicApiRequest request, java.util.Map<String, String> pathVariables) {
        String url = config.getUrl().trim();
        String expanded = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(pathVariables == null ? java.util.Collections.emptyMap() : pathVariables)
                .toUriString();
        if (config.isPassQuery() && request != null && request.getQueryParams() != null) {
            Map<String, List<String>> queryParams = request.getQueryParams();
            if (!queryParams.isEmpty()) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(expanded);
                for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    if (values == null || values.isEmpty()) {
                        builder.queryParam(key);
                    } else {
                        for (String value : values) {
                            builder.queryParam(key, value);
                        }
                    }
                }
                return builder.toUriString();
            }
        }
        return expanded;
    }

    private HttpHeaders buildHeaders(HttpForwardConfig config, DynamicApiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (request != null && config.isPassHeaders()) {
            Map<String, String> requestHeaders = request.getHeaders();
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null) {
                    continue;
                }
                if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                    continue;
                }
                headers.add(name, entry.getValue());
            }
        }
        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            for (java.util.Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    headers.set(entry.getKey(), entry.getValue());
                }
            }
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE) && request != null) {
            String contentType = null;
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                    contentType = entry.getValue();
                    break;
                }
            }
            if (contentType != null) {
                headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            }
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
