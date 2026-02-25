package com.example.demo.config.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置种子开关（config.seed.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "config.seed")
public class ConfigSeedProperties {

    /**
     * 是否启用配置种子。
     * 默认开启，且仅对标记了 seed 的字段生效。
     */
    private boolean enabled = true;
}
