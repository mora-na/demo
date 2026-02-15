package com.example.demo.common.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 缓存配置项，绑定 cache.*。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    /**
     * 缓存位置：redis | memory | db。
     */
    private String location = "redis";

    private Memory memory = new Memory();

    private Db db = new Db();

    public CacheLocation getLocationEnum() {
        return CacheLocation.from(location);
    }

    @Data
    public static class Memory {
        /**
         * 内存缓存最大条目数。
         */
        private long maximumSize = 10_000;

        /**
         * 最大权重（MB，按估算内存权重限制），0 表示不启用权重限制。
         */
        private long maximumWeightMb = 64;

        /**
         * 清理间隔（秒），0 表示关闭定时清理。
         */
        private long cleanupIntervalSeconds = 60;
    }

    @Data
    public static class Db {
        /**
         * 数据库缓存最大行数，0 表示不启用限制。
         */
        private long maximumRows = 100_000;

        /**
         * 清理间隔（秒），0 表示关闭定时清理。
         */
        private long cleanupIntervalSeconds = 300;
    }
}
