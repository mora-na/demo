package com.example.demo.common.web.limit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 限流配置项，绑定 security.rate-limit 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private long windowSeconds = 60;

    private int maxRequests = 100;

    private String keyMode = "ip";

    private boolean includePath = true;

    private List<String> excludePaths = new ArrayList<>();

    /**
     * 是否启用限流。
     *
     * @return true 表示启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用限流。
     *
     * @param enabled 是否启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取限流时间窗口（秒）。
     *
     * @return 时间窗口（秒）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public long getWindowSeconds() {
        return windowSeconds;
    }

    /**
     * 设置限流时间窗口（秒）。
     *
     * @param windowSeconds 时间窗口（秒）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    /**
     * 获取窗口内最大请求数。
     *
     * @return 最大请求数
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public int getMaxRequests() {
        return maxRequests;
    }

    /**
     * 设置窗口内最大请求数。
     *
     * @param maxRequests 最大请求数
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    /**
     * 获取限流 Key 生成模式。
     *
     * @return Key 模式
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public String getKeyMode() {
        return keyMode;
    }

    /**
     * 设置限流 Key 生成模式。
     *
     * @param keyMode Key 模式
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setKeyMode(String keyMode) {
        this.keyMode = keyMode;
    }

    /**
     * 是否在限流 Key 中包含路径。
     *
     * @return true 表示包含
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public boolean isIncludePath() {
        return includePath;
    }

    /**
     * 设置是否在限流 Key 中包含路径。
     *
     * @param includePath 是否包含路径
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setIncludePath(boolean includePath) {
        this.includePath = includePath;
    }

    /**
     * 获取限流排除路径列表。
     *
     * @return 排除路径列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public List<String> getExcludePaths() {
        return excludePaths;
    }

    /**
     * 设置限流排除路径列表。
     *
     * @param excludePaths 排除路径列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
