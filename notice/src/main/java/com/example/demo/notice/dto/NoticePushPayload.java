package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知推送载荷，用于 SSE 下发。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticePushPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long noticeId;

    private String title;

    private String createdName;

    private LocalDateTime createdAt;

    private Long unreadCount;

    private java.util.List<NoticeLatestVO> latestNotices;

    private Long heartbeatIntervalMillis;

    private Long heartbeatTimeoutMillis;

    /**
     * 断线后建议重连间隔（毫秒）。
     */
    private Long retryAfterMillis;

    /**
     * 流状态标记，例如 rejected。
     */
    private String streamStatus;
}
