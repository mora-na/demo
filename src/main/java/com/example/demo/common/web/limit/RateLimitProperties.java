package com.example.demo.common.web.limit;

import lombok.Getter;
import lombok.Setter;
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
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    /**
     * -- GETTER --
     * 是否启用限流。
     *
     * @return true 表示启用
     * <p>
     * -- SETTER --
     * 设置是否启用限流。
     * @param enabled 是否启用
     *
     */
    private boolean enabled = true;

    /**
     * -- GETTER --
     * 获取限流时间窗口（秒）。
     *
     * @return 时间窗口（秒）
     * <p>
     * -- SETTER --
     * 设置限流时间窗口（秒）。
     * @param windowSeconds 时间窗口（秒）
     *
     */
    private long windowSeconds = 60;

    /**
     * -- GETTER --
     * 获取窗口内最大请求数。
     *
     * @return 最大请求数
     * <p>
     * -- SETTER --
     * 设置窗口内最大请求数。
     * @param maxRequests 最大请求数
     *
     */
    private int maxRequests = 100;

    /**
     * -- GETTER --
     * 获取限流 Key 生成模式。
     *
     * @return Key 模式
     * <p>
     * -- SETTER --
     * 设置限流 Key 生成模式。
     * @param keyMode Key 模式
     *
     */
    private String keyMode = "ip";

    /**
     * -- GETTER --
     * 是否在限流 Key 中包含路径。
     *
     * @return true 表示包含
     * <p>
     * -- SETTER --
     * 设置是否在限流 Key 中包含路径。
     * @param includePath 是否包含路径
     *
     */
    private boolean includePath = true;

    /**
     * -- GETTER --
     * 获取限流排除路径列表。
     *
     * @return 排除路径列表
     * <p>
     * -- SETTER --
     * 设置限流排除路径列表。
     * @param excludePaths 排除路径列表
     *
     */
    private List<String> excludePaths = new ArrayList<>();

}
