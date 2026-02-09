package com.example.demo.common.web.limit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.duplicate-submit")
public class DuplicateSubmitProperties {

    private boolean enabled = true;

    private long intervalMillis = 3000;

    private String keyMode = "ip";

    private boolean includePath = true;

    private boolean useIdempotencyKey = true;

    private String headerName = "Idempotency-Key";

    private boolean includeBody = true;

    private List<String> methods = new ArrayList<>(Arrays.asList("POST", "PUT", "PATCH", "DELETE"));

    private List<String> excludePaths = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public void setIntervalMillis(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public String getKeyMode() {
        return keyMode;
    }

    public void setKeyMode(String keyMode) {
        this.keyMode = keyMode;
    }

    public boolean isIncludePath() {
        return includePath;
    }

    public void setIncludePath(boolean includePath) {
        this.includePath = includePath;
    }

    public boolean isUseIdempotencyKey() {
        return useIdempotencyKey;
    }

    public void setUseIdempotencyKey(boolean useIdempotencyKey) {
        this.useIdempotencyKey = useIdempotencyKey;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
