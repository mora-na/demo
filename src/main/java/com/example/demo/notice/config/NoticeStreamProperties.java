package com.example.demo.notice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知 SSE 配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@Component
@ConfigurationProperties(prefix = "notice.sse")
public class NoticeStreamProperties {

    /**
     * 心跳间隔（毫秒），小于等于 0 则禁用心跳。
     */
    private long heartbeatIntervalMillis = 30000L;

    /**
     * 断线判定超时（毫秒），用于前端显示连接状态。
     */
    private long heartbeatTimeoutMillis = 90000L;

    /**
     * 推送最新通知列表长度。
     */
    private int latestLimit = 5;
}
