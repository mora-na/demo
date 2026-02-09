package com.example.demo.common.web.limit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 重复提交防护配置项，绑定 security.duplicate-submit 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
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

    /**
     * 是否启用重复提交防护。
     *
     * @return true 表示启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用重复提交防护。
     *
     * @param enabled 是否启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取重复提交判定间隔（毫秒）。
     *
     * @return 判定间隔（毫秒）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public long getIntervalMillis() {
        return intervalMillis;
    }

    /**
     * 设置重复提交判定间隔（毫秒）。
     *
     * @param intervalMillis 判定间隔（毫秒）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setIntervalMillis(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    /**
     * 获取重复提交 Key 模式。
     *
     * @return Key 模式
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public String getKeyMode() {
        return keyMode;
    }

    /**
     * 设置重复提交 Key 模式。
     *
     * @param keyMode Key 模式
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setKeyMode(String keyMode) {
        this.keyMode = keyMode;
    }

    /**
     * 是否在重复提交 Key 中包含路径。
     *
     * @return true 表示包含
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public boolean isIncludePath() {
        return includePath;
    }

    /**
     * 设置是否在重复提交 Key 中包含路径。
     *
     * @param includePath 是否包含路径
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setIncludePath(boolean includePath) {
        this.includePath = includePath;
    }

    /**
     * 是否启用幂等键 Header。
     *
     * @return true 表示启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public boolean isUseIdempotencyKey() {
        return useIdempotencyKey;
    }

    /**
     * 设置是否启用幂等键 Header。
     *
     * @param useIdempotencyKey 是否启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setUseIdempotencyKey(boolean useIdempotencyKey) {
        this.useIdempotencyKey = useIdempotencyKey;
    }

    /**
     * 获取幂等键 Header 名称。
     *
     * @return Header 名称
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * 设置幂等键 Header 名称。
     *
     * @param headerName Header 名称
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * 是否在 Key 中包含请求体摘要。
     *
     * @return true 表示包含
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public boolean isIncludeBody() {
        return includeBody;
    }

    /**
     * 设置是否在 Key 中包含请求体摘要。
     *
     * @param includeBody 是否包含请求体
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    /**
     * 获取需要保护的 HTTP 方法列表。
     *
     * @return 方法列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public List<String> getMethods() {
        return methods;
    }

    /**
     * 设置需要保护的 HTTP 方法列表。
     *
     * @param methods 方法列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    /**
     * 获取重复提交排除路径列表。
     *
     * @return 排除路径列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public List<String> getExcludePaths() {
        return excludePaths;
    }

    /**
     * 设置重复提交排除路径列表。
     *
     * @param excludePaths 排除路径列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
