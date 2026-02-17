package com.example.demo.extension.support;

import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.web.filter.CachedBodyHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 动态接口请求提取器。
 */
@Component
public class DynamicApiRequestExtractor {

    private final ObjectMapper objectMapper;
    private final CommonConstants commonConstants;

    public DynamicApiRequestExtractor(ObjectMapper objectMapper, CommonConstants commonConstants) {
        this.objectMapper = objectMapper;
        this.commonConstants = commonConstants;
    }

    public DynamicApiRequest extract(HttpServletRequest request, Map<String, String> pathVariables) {
        if (request == null) {
            return DynamicApiRequest.builder().build();
        }
        String path = request.getRequestURI();
        String method = request.getMethod();
        Map<String, String> headers = resolveHeaders(request);
        String rawBody = resolveBody(request);
        Object body = parseBody(rawBody, request.getContentType());
        Map<String, Object> params = mergeParams(pathVariables, request.getParameterMap(), body);
        return DynamicApiRequest.builder()
                .rawRequest(request)
                .path(path)
                .method(method)
                .headers(headers)
                .rawBody(rawBody)
                .body(body)
                .params(params)
                .pathVariables(pathVariables)
                .build();
    }

    private Map<String, Object> mergeParams(Map<String, String> pathVariables,
                                            Map<String, String[]> queryParams,
                                            Object body) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (pathVariables != null) {
            merged.putAll(pathVariables);
        }
        if (queryParams != null) {
            for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                if (values == null) {
                    merged.put(key, null);
                } else if (values.length == 1) {
                    merged.put(key, values[0]);
                } else {
                    merged.put(key, Arrays.asList(values));
                }
            }
        }
        if (body instanceof Map) {
            Map<?, ?> bodyMap = (Map<?, ?>) body;
            for (Map.Entry<?, ?> entry : bodyMap.entrySet()) {
                if (entry.getKey() != null) {
                    merged.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        } else if (body != null) {
            merged.put("body", body);
        }
        return merged;
    }

    private String resolveBody(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String contentType = request.getContentType();
        String multipartPrefix = commonConstants.getHttp().getMultipartPrefix();
        if (contentType != null && multipartPrefix != null
                && contentType.toLowerCase(Locale.ROOT).startsWith(multipartPrefix.toLowerCase(Locale.ROOT))) {
            return null;
        }
        try {
            byte[] bytes;
            if (request instanceof CachedBodyHttpServletRequest) {
                bytes = ((CachedBodyHttpServletRequest) request).getCachedBody();
            } else {
                bytes = StreamUtils.copyToByteArray(request.getInputStream());
            }
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            Charset charset = resolveCharset(request.getCharacterEncoding());
            return new String(bytes, charset);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object parseBody(String rawBody, String contentType) {
        if (StringUtils.isBlank(rawBody)) {
            return null;
        }
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("json")) {
            try {
                return objectMapper.readValue(rawBody, Object.class);
            } catch (Exception ignored) {
                return rawBody;
            }
        }
        return rawBody;
    }

    private Map<String, String> resolveHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return headers;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }
        return headers;
    }

    private Charset resolveCharset(String encoding) {
        if (encoding == null) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (Exception ex) {
            return StandardCharsets.UTF_8;
        }
    }
}
