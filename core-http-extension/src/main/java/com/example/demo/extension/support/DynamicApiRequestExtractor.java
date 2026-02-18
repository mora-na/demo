package com.example.demo.extension.support;

import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.web.filter.CachedBodyHttpServletRequest;
import com.example.demo.extension.api.request.DynamicApiParamMode;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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

    public DynamicApiRequest extract(HttpServletRequest request,
                                     Map<String, String> pathVariables,
                                     DynamicApiParamMode paramMode) {
        if (request == null) {
            return DynamicApiRequest.builder().build();
        }
        DynamicApiParamMode resolvedMode = paramMode == null ? DynamicApiParamMode.AUTO : paramMode;
        String path = request.getRequestURI();
        String method = request.getMethod();
        Map<String, String> headers = resolveHeaders(request);
        String rawBody = resolveBody(request);
        Object body = parseBody(rawBody, request.getContentType());
        Map<String, List<String>> queryParams = resolveQueryParams(request.getParameterMap());
        Map<String, Object> params = mergeParams(resolvedMode, pathVariables, queryParams, body);
        Map<String, MultipartFile> fileMap = resolveFileMap(request);
        Map<String, List<MultipartFile>> multiFileMap = resolveMultiFileMap(request);
        return DynamicApiRequest.builder()
                .path(path)
                .method(method)
                .headers(headers)
                .rawBody(rawBody)
                .body(body)
                .params(params)
                .pathVariables(pathVariables)
                .queryParams(queryParams)
                .fileMap(fileMap)
                .multiFileMap(multiFileMap)
                .paramMode(resolvedMode)
                .build();
    }

    private Map<String, Object> mergeParams(DynamicApiParamMode paramMode,
                                            Map<String, String> pathVariables,
                                            Map<String, List<String>> queryParams,
                                            Object body) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (pathVariables != null) {
            merged.putAll(pathVariables);
        }
        if (paramMode == DynamicApiParamMode.QUERY || paramMode == DynamicApiParamMode.FORM) {
            mergeQueryParams(merged, queryParams);
            return merged;
        }
        if (paramMode == DynamicApiParamMode.BODY_JSON) {
            mergeBody(merged, body);
            return merged;
        }
        if (paramMode == DynamicApiParamMode.MULTIPART) {
            mergeQueryParams(merged, queryParams);
            return merged;
        }
        mergeQueryParams(merged, queryParams);
        mergeBody(merged, body);
        return merged;
    }

    private void mergeQueryParams(Map<String, Object> merged, Map<String, List<String>> queryParams) {
        if (queryParams == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                merged.put(key, null);
            } else if (values.size() == 1) {
                merged.put(key, values.get(0));
            } else {
                merged.put(key, values);
            }
        }
    }

    private void mergeBody(Map<String, Object> merged, Object body) {
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
    }

    private Map<String, List<String>> resolveQueryParams(Map<String, String[]> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values == null) {
                resolved.put(key, Collections.emptyList());
            } else {
                resolved.put(key, Arrays.asList(values));
            }
        }
        return resolved;
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

    private Map<String, MultipartFile> resolveFileMap(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return Collections.emptyMap();
        }
        MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipart.getFileMap();
        if (fileMap == null || fileMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(fileMap);
    }

    private Map<String, List<MultipartFile>> resolveMultiFileMap(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return Collections.emptyMap();
        }
        MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiValueMap = multipart.getMultiFileMap();
        if (multiValueMap == null || multiValueMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<MultipartFile>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<MultipartFile>> entry : multiValueMap.entrySet()) {
            List<MultipartFile> files = entry.getValue();
            if (files == null) {
                result.put(entry.getKey(), Collections.emptyList());
            } else {
                result.put(entry.getKey(), new ArrayList<>(files));
            }
        }
        return result;
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
