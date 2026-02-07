package com.example.demo.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "user")
public class UserConfig {

    /**
     * 承载 user.xxx 下的所有配置
     */
    private Map<String, Object> config;

}
