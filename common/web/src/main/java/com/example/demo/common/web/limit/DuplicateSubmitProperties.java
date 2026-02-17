package com.example.demo.common.web.limit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
@ConfigurationProperties(prefix = "security.duplicate-submit")
public class DuplicateSubmitProperties {

    /**
     * -- GETTER --
     * 是否启用重复提交防护。
     *
     * @return true 表示启用
     * <p>
     * -- SETTER --
     * 设置是否启用重复提交防护。
     * @param enabled 是否启用
     *
     */
    private boolean enabled = true;

    /**
     * -- GETTER --
     * 获取重复提交判定间隔（毫秒）。
     *
     * @return 判定间隔（毫秒）
     * <p>
     * -- SETTER --
     * 设置重复提交判定间隔（毫秒）。
     * @param intervalMillis 判定间隔（毫秒）
     *
     */
    private long intervalMillis = 3000;

    /**
     * -- GETTER --
     * 获取重复提交 Key 模式。
     *
     * @return Key 模式
     * <p>
     * -- SETTER --
     * 设置重复提交 Key 模式。
     * @param keyMode Key 模式
     *
     */
    private String keyMode = "ip";

    /**
     * -- GETTER --
     * 是否在重复提交 Key 中包含路径。
     *
     * @return true 表示包含
     * <p>
     * -- SETTER --
     * 设置是否在重复提交 Key 中包含路径。
     * @param includePath 是否包含路径
     *
     */
    private boolean includePath = true;

    /**
     * -- GETTER --
     * 是否启用幂等键 Header。
     *
     * @return true 表示启用
     * <p>
     * -- SETTER --
     * 设置是否启用幂等键 Header。
     * @param useIdempotencyKey 是否启用
     *
     */
    private boolean useIdempotencyKey = true;

    /**
     * -- GETTER --
     * 获取幂等键 Header 名称。
     *
     * @return Header 名称
     * <p>
     * -- SETTER --
     * 设置幂等键 Header 名称。
     * @param headerName Header 名称
     *
     */
    private String headerName = "Idempotency-Key";

    /**
     * -- GETTER --
     * 是否在 Key 中包含请求体摘要。
     *
     * @return true 表示包含
     * <p>
     * -- SETTER --
     * 设置是否在 Key 中包含请求体摘要。
     * @param includeBody 是否包含请求体
     *
     */
    private boolean includeBody = true;

    /**
     * -- GETTER --
     * 获取需要保护的 HTTP 方法列表。
     *
     * @return 方法列表
     * <p>
     * -- SETTER --
     * 设置需要保护的 HTTP 方法列表。
     * @param methods 方法列表
     *
     */
    private List<String> methods = new ArrayList<>(Arrays.asList("POST", "PUT", "PATCH", "DELETE"));

    /**
     * -- GETTER --
     * 获取重复提交排除路径列表。
     *
     * @return 排除路径列表
     * <p>
     * -- SETTER --
     * 设置重复提交排除路径列表。
     * @param excludePaths 排除路径列表
     *
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * -- GETTER --
     * 获取重复提交额外排除路径列表。
     *
     * @return 额外排除路径列表
     * <p>
     * -- SETTER --
     * 设置重复提交额外排除路径列表。
     * @param additionalExcludePaths 额外排除路径列表
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

}
