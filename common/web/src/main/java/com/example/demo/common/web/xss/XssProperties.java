package com.example.demo.common.web.xss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * XSS 防护配置项，绑定 security.xss 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "security.xss")
public class XssProperties {

    private boolean enabled = true;

    /**
     * XSS 清洗递归最大深度。
     */
    @Min(0)
    private int maxScanDepth = 8;

    /**
     * XSS 清洗最大节点数。
     */
    @Min(0)
    private int maxScanNodes = 10000;

    private List<String> excludePaths = new ArrayList<>();

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
