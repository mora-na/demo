package com.example.demo.extension.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 动态限流策略列表（可选）。
     */
    private List<RateLimitPolicy> rateLimitPolicies = new ArrayList<>();

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

    @Data
    public static class RateLimitPolicy {
        private String id;
        private String name;
        private long windowSeconds;
        private int maxRequests;
        private String keyMode;
        private boolean includePath = true;
    }
}
