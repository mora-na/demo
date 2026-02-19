package com.example.demo.extension.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态接口扩展配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamic.api")
public class DynamicApiProperties {

    private Global global = new Global();

    /**
     * 执行线程池配置。
     */
    private Executor executor = new Executor();

    /**
     * 自定义执行器配置（按名称）。
     */
    private Map<String, Executor> executors = new LinkedHashMap<>();

    /**
     * 执行器路由规则（按 api/type/path 分流）。
     */
    private List<ExecutorRoute> executorRoutes = new ArrayList<>();

    /**
     * 动态接口熔断配置。
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * 动态接口指标配置。
     */
    private Metrics metrics = new Metrics();

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
        /**
         * 拒绝策略：ABORT / CALLER_RUNS / DISCARD / DISCARD_OLDEST。
         */
        private String rejectedPolicy = "ABORT";
    }

    @Data
    public static class ExecutorRoute {
        /**
         * 关联的执行器名称（必须在 executors 中定义）。
         */
        private String executorId;
        /**
         * 动态接口类型匹配（可选）。
         */
        private String type;
        /**
         * 动态接口 API ID 匹配（可选）。
         */
        private Long apiId;
        /**
         * 路径前缀匹配（可选）。
         */
        private String pathPrefix;
    }

    @Data
    public static class CircuitBreaker {
        private boolean enabled = false;
        private int windowSeconds = 60;
        private int minimumCalls = 20;
        /**
         * 失败率阈值（0-1）。
         */
        private double failureRate = 0.5d;
        private long openDurationMs = 30000L;
    }

    @Data
    public static class Metrics {
        private boolean enabled = true;
        /**
         * 返回明细上限，防止接口过多导致结果过大。
         */
        private int maxDetails = 200;
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
