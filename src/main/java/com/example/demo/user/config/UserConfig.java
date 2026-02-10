package com.example.demo.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户配置属性，绑定 user.* 下的键值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "user")
public class UserConfig {

    /**
     * 承载 user.xxx 下的所有配置
     */
    private Map<String, Object> config;

}
