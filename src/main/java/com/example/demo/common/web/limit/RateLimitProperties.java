package com.example.demo.common.web.limit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private long windowSeconds = 60;

    private int maxRequests = 100;

    private String keyMode = "ip";

    private boolean includePath = true;

    private int maxCacheSize = 10000;

    private List<String> excludePaths = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
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

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
