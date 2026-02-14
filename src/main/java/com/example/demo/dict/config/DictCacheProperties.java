package com.example.demo.dict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 字典缓存配置，绑定 dict.cache.*。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
@Component
@ConfigurationProperties(prefix = "dict.cache")
public class DictCacheProperties {

    /**
     * 字典缓存时长（秒），<=0 表示不缓存。
     */
    private long seconds = 600;
}
