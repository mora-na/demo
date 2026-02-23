package com.example.demo.common.web.limit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 限流配置项，绑定 security.rate-limit 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Setter
@Getter
@Component
@Validated
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    /**
     * -- GETTER --
     * 是否启用限流。
     *
     *
     */
    private boolean enabled = true;

    /**
     * -- GETTER --
     * 获取限流时间窗口（秒）。
     *
     *
     */
    @Min(0)
    private long windowSeconds = 60;

    /**
     * -- GETTER --
     * 获取窗口内最大请求数。
     *
     *
     */
    @Min(0)
    private int maxRequests = 100;

    /**
     * -- GETTER --
     * 获取限流 Key 生成模式。
     *
     *
     */
    private String keyMode = "ip";

    /**
     * -- GETTER --
     * 是否在限流 Key 中包含路径。
     *
     *
     */
    private boolean includePath = true;

    /**
     * -- GETTER --
     * 获取限流排除路径列表。
     *
     *
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * -- GETTER --
     * 获取限流额外排除路径列表。
     *
     *
     */
    private List<String> additionalExcludePaths = new ArrayList<>();

    private static List<String> mergeExcludePaths(List<String> base, List<String> additional) {
        boolean emptyBase = base == null || base.isEmpty();
        boolean emptyAdditional = additional == null || additional.isEmpty();
        if (emptyBase && emptyAdditional) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (!emptyBase) {
            merged.addAll(base);
        }
        if (!emptyAdditional) {
            merged.addAll(additional);
        }
        return new ArrayList<>(merged);
    }

    public List<String> getExcludePaths() {
        return mergeExcludePaths(excludePaths, additionalExcludePaths);
    }

    @AssertTrue(message = "security.rate-limit.window-seconds and max-requests must be > 0 when enabled")
    public boolean isValidLimitConfig() {
        if (!enabled) {
            return true;
        }
        return windowSeconds > 0 && maxRequests > 0;
    }

}
