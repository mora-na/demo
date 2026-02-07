package com.example.demo.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "user")
public class UserConfig {

    public static Map<String, Object> CONFIG;
    /**
     * 承载 user.xxx 下的所有配置
     */
    public Map<String, Object> config;

    @PostConstruct
    public void init() {
        CONFIG = this.config;
    }

}
