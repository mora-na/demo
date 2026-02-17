package com.example.demo.extension.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 动态接口扩展配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamic.api")
public class DynamicApiProperties {

    private Global global = new Global();

    /**
     * 默认超时（毫秒）。
     */
    private long defaultTimeoutMs = 3000L;

    private Executor executor = new Executor();

    @Data
    public static class Global {
        private boolean enabled = true;
    }

    @Data
    public static class Executor {
        private int corePoolSize = 8;
        private int maxPoolSize = 16;
        private int queueCapacity = 200;
        private int keepAliveSeconds = 60;
        private String threadNamePrefix = "ext-exec-";
    }
}
