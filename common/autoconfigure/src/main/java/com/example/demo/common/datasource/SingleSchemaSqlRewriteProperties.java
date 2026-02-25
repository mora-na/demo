package com.example.demo.common.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 单 schema SQL 重写配置。
 */
@Data
@ConfigurationProperties(prefix = "app.datasource.sql-rewrite")
public class SingleSchemaSqlRewriteProperties {

    private boolean enabled;

    private String targetSchema = "demo";

    private List<String> sourceSchemas = new ArrayList<>(Arrays.asList(
            "demo_system", "demo_config", "demo_notice", "demo_job", "demo_log",
            "demo_dict", "demo_cache", "demo_extension", "demo_order"
    ));

    private int cacheSize = 512;

    private boolean fallbackToRegex = true;
}
