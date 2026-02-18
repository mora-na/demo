package com.example.demo.extension.registry;

import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import lombok.Getter;
import org.springframework.web.util.pattern.PathPattern;

/**
 * 动态接口运行时元数据。
 */
@Getter
public class DynamicApiMeta {

    private final DynamicApi api;
    private final String type;
    private final DynamicApiAuthMode authMode;
    private final Object config;
    private final PathPattern pathPattern;

    public DynamicApiMeta(DynamicApi api,
                          String type,
                          DynamicApiAuthMode authMode,
                          Object config,
                          PathPattern pathPattern) {
        this.api = api;
        this.type = type;
        this.authMode = authMode;
        this.config = config;
        this.pathPattern = pathPattern;
    }

    public boolean isPattern() {
        return pathPattern != null && pathPattern.getPatternString() != null
                && !pathPattern.getPatternString().equals(api.getPath());
    }
}
