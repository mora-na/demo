package com.example.demo.config.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认配置项（config.defaults.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "config.defaults")
public class ConfigDefaultsProperties {

    /**
     * 默认配置项，支持 key 或 group.key 形式。
     */
    private Map<String, String> items = new LinkedHashMap<>();
}
