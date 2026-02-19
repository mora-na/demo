package com.example.demo.common.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 模块数据源路由配置。
 */
@Data
@ConfigurationProperties(prefix = "app.datasource.routing")
public class ModuleDataSourceRoutingProperties {

    private ReadOnlyDetection readOnlyDetection = ReadOnlyDetection.EXPLICIT;

    private List<String> readOnlyMethodPrefixes = new ArrayList<String>(Arrays.asList(
            "get", "find", "list", "page", "query", "select", "count",
            "search", "load", "read", "fetch", "resolve", "exists"
    ));

    private List<ModuleConfig> modules = new ArrayList<ModuleConfig>();

    public enum ReadOnlyDetection {
        EXPLICIT,
        EXPLICIT_OR_METHOD_NAME
    }

    @Data
    public static class ModuleConfig {
        private String name;
        private String rwDataSource;
        private String roDataSource;
        private boolean forceReadWrite;
        private List<String> packages = new ArrayList<String>();
        private List<String> classes = new ArrayList<String>();
    }
}
