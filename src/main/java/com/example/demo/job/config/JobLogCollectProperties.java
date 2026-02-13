package com.example.demo.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定时任务日志收集配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@Component
@ConfigurationProperties(prefix = "job.log.collect")
public class JobLogCollectProperties {

    /**
     * 是否启用自动日志收集。
     */
    private boolean enabled = true;

    /**
     * 收集范围：MDC | THREAD。
     * MDC: 仅收集含 MDC 标识的日志（默认）。
     * THREAD: 仅收集线程名匹配的日志。
     */
    private Scope scope = Scope.MDC;

    /**
     * 最低收集级别：TRACE/DEBUG/INFO/WARN/ERROR。
     */
    private String minLevel = "INFO";

    /**
     * 单次执行最大日志长度（字符）。
     */
    private int maxLength = 65536;

    /**
     * 延迟合并时间（毫秒），用于等待异步日志。
     */
    private long mergeDelayMillis = 3000L;

    /**
     * 最大缓冲保留时间（毫秒），用于清理长时间未合并的缓冲。
     */
    private long maxHoldMillis = 60000L;

    /**
     * 是否启用 InheritableThreadLocal 兜底（未使用 MDC 时也可透传）。
     */
    private boolean inheritThreadContext = true;

    /**
     * MDC 关联键。
     */
    private String mdcKey = "jobLogId";

    /**
     * 线程名 MDC 键（scope=THREAD 时使用）。
     */
    private String threadKey = "jobLogThread";

    public enum Scope {
        MDC,
        THREAD
    }
}
