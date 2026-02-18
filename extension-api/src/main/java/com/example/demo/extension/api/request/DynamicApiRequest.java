package com.example.demo.extension.api.request;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 动态接口请求上下文。
 */
@Getter
@Builder
public class DynamicApiRequest {

    private final String path;
    private final String method;
    private final Map<String, Object> params;
    private final Map<String, String> pathVariables;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> headers;
    private final Object body;
    private final String rawBody;
    private final Map<String, MultipartFile> fileMap;
    private final Map<String, List<MultipartFile>> multiFileMap;
    private final DynamicApiParamMode paramMode;

    private static String abbreviate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "...(truncated)";
    }

    public Map<String, Object> getParams() {
        return params == null ? Collections.emptyMap() : params;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables == null ? Collections.emptyMap() : pathVariables;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams == null ? Collections.emptyMap() : queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers == null ? Collections.emptyMap() : headers;
    }

    public Map<String, MultipartFile> getFileMap() {
        return fileMap == null ? Collections.emptyMap() : fileMap;
    }

    public Map<String, List<MultipartFile>> getMultiFileMap() {
        return multiFileMap == null ? Collections.emptyMap() : multiFileMap;
    }

    public String getParam(String name) {
        Object value = getParams().get(name);
        return value == null ? null : String.valueOf(value);
    }

    public String getPathVariable(String name) {
        return getPathVariables().get(name);
    }

    public List<String> getQueryParam(String name) {
        return getQueryParams().get(name);
    }

    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    public MultipartFile getFile(String name) {
        return getFileMap().get(name);
    }

    public List<MultipartFile> getFiles(String name) {
        return getMultiFileMap().get(name);
    }

    public String getFirstQueryParam(String name) {
        List<String> values = getQueryParam(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    public List<String> getHeaderKeys() {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>(headers.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }

    public boolean hasFiles() {
        return (fileMap != null && !fileMap.isEmpty()) || (multiFileMap != null && !multiFileMap.isEmpty());
    }

    public List<FileInfo> getFileInfos() {
        List<FileInfo> infos = new ArrayList<>();
        for (Map.Entry<String, MultipartFile> entry : getFileMap().entrySet()) {
            infos.add(FileInfo.of(entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<String, List<MultipartFile>> entry : getMultiFileMap().entrySet()) {
            List<MultipartFile> files = entry.getValue();
            if (files == null || files.isEmpty()) {
                continue;
            }
            int index = 0;
            for (MultipartFile file : files) {
                infos.add(FileInfo.of(entry.getKey() + "[" + index + "]", file));
                index++;
            }
        }
        return infos;
    }

    public String getFileSummary() {
        List<FileInfo> infos = getFileInfos();
        if (infos.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < infos.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(infos.get(i).toSummary());
        }
        builder.append(']');
        return builder.toString();
    }

    public String abbreviateRawBody(int maxLength) {
        return abbreviate(rawBody, maxLength);
    }

    @Getter
    public static class FileInfo {
        private final String fieldName;
        private final String originalFilename;
        private final long size;
        private final String contentType;

        private FileInfo(String fieldName, String originalFilename, long size, String contentType) {
            this.fieldName = fieldName;
            this.originalFilename = originalFilename;
            this.size = size;
            this.contentType = contentType;
        }

        public static FileInfo of(String fieldName, MultipartFile file) {
            if (file == null) {
                return new FileInfo(fieldName, "unknown", -1L, null);
            }
            String filename = file.getOriginalFilename();
            if (filename == null || filename.trim().isEmpty()) {
                filename = "unknown";
            }
            return new FileInfo(fieldName, filename, file.getSize(), file.getContentType());
        }

        public String toSummary() {
            if (contentType == null || contentType.trim().isEmpty()) {
                return fieldName + "->" + originalFilename + "(" + size + "B)";
            }
            return fieldName + "->" + originalFilename + "(" + size + "B, " + contentType + ")";
        }
    }
}
