package com.example.demo.extension.registry;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * 动态接口匹配结果。
 */
@Getter
public class DynamicApiMatch {

    private final DynamicApiMeta meta;
    private final Map<String, String> pathVariables;

    public DynamicApiMatch(DynamicApiMeta meta, Map<String, String> pathVariables) {
        this.meta = meta;
        this.pathVariables = pathVariables == null ? Collections.emptyMap() : pathVariables;
    }
}
