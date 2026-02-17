package com.example.demo.common.web.permission;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 权限配置项，绑定 security.permission 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.permission")
public class PermissionProperties {

    private String source = "db";

    private boolean enabled = true;

    private boolean requireLoginByDefault = false;

    private List<String> superUsers = new ArrayList<>();

    private Map<String, List<String>> userPermissions = new LinkedHashMap<>();

    private List<String> excludePaths = new ArrayList<>();

    private List<String> additionalExcludePaths = new ArrayList<>();

    private long cacheSeconds = 0;

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
