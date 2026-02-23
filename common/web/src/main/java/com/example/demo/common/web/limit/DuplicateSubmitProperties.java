package com.example.demo.common.web.limit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.util.*;

/**
 * 重复提交防护配置项，绑定 security.duplicate-submit 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Setter
@Getter
@Component
@Validated
@ConfigurationProperties(prefix = "security.duplicate-submit")
public class DuplicateSubmitProperties {

    /**
     * -- GETTER --
     * 是否启用重复提交防护。
     *
     *
     */
    private boolean enabled = true;

    /**
     * -- GETTER --
     * 获取重复提交判定间隔（毫秒）。
     *
     *
     */
    @Min(0)
    private long intervalMillis = 3000;

    /**
     * -- GETTER --
     * 获取重复提交 Key 模式。
     *
     *
     */
    private String keyMode = "ip";

    /**
     * -- GETTER --
     * 是否在重复提交 Key 中包含路径。
     *
     *
     */
    private boolean includePath = true;

    /**
     * -- GETTER --
     * 是否启用幂等键 Header。
     *
     *
     */
    private boolean useIdempotencyKey = true;

    /**
     * -- GETTER --
     * 获取幂等键 Header 名称。
     *
     *
     */
    private String headerName = "Idempotency-Key";

    /**
     * -- GETTER --
     * 是否在 Key 中包含请求体摘要。
     *
     *
     */
    private boolean includeBody = true;

    /**
     * -- GETTER --
     * 获取允许参与请求体摘要的最大字节数（<=0 表示不限制）。
     *
     *
     */
    @Min(0)
    private int maxBodyBytes = 64 * 1024;

    /**
     * -- GETTER --
     * 获取需要保护的 HTTP 方法列表。
     *
     *
     */
    private List<String> methods = new ArrayList<>(Arrays.asList("POST", "PUT", "PATCH", "DELETE"));

    /**
     * -- GETTER --
     * 获取重复提交排除路径列表。
     *
     *
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * -- GETTER --
     * 获取重复提交额外排除路径列表。
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

    @AssertTrue(message = "security.duplicate-submit.interval-millis must be > 0 when enabled")
    public boolean isValidIntervalConfig() {
        if (!enabled) {
            return true;
        }
        return intervalMillis > 0;
    }

}
