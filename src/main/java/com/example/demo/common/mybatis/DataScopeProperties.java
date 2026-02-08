package com.example.demo.common.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "security.data-scope")
public class DataScopeProperties {

    private boolean enabled = true;

    private String source = "db";

    private String defaultType = DataScopeType.ALL;

    private long cacheSeconds = 0;

    private Map<String, String> tableColumnMap = new LinkedHashMap<>();

}
