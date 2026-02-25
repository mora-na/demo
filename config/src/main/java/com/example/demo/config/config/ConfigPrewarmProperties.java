package com.example.demo.config.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置缓存预热开关与范围（config.prewarm.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "config.prewarm")
public class ConfigPrewarmProperties {

    /**
     * 是否启用配置缓存预热。
     */
    private boolean enabled = true;

    /**
     * 预热范围：
     * ALL / SEEDED / HOT / SEEDED_OR_HOT / NONE
     */
    private PrewarmMode mode = PrewarmMode.ALL;

    public enum PrewarmMode {
        ALL,
        SEEDED,
        HOT,
        SEEDED_OR_HOT,
        NONE
    }
}
