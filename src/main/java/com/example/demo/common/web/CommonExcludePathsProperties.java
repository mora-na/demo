package com.example.demo.common.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "security.common")
public class CommonExcludePathsProperties {

    private List<String> excludePaths = new ArrayList<>();

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
