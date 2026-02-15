package com.example.demo.common.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 公共排除路径配置，统一管理过滤器/拦截器的排除规则。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.common")
public class CommonExcludePathsProperties {

    private List<String> excludePaths = new ArrayList<>();

    /**
     * 合并公共排除路径与模块额外排除路径，保持去重与顺序。
     *
     * @param extraExcludePaths 模块额外排除路径
     * @return 合并后的排除路径列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public List<String> merge(List<String> extraExcludePaths) {
        boolean emptyCommon = excludePaths == null || excludePaths.isEmpty();
        boolean emptyExtra = extraExcludePaths == null || extraExcludePaths.isEmpty();
        if (emptyCommon && emptyExtra) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (!emptyCommon) {
            merged.addAll(excludePaths);
        }
        if (!emptyExtra) {
            merged.addAll(extraExcludePaths);
        }
        return new ArrayList<>(merged);
    }
}
