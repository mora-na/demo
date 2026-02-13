package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 最新通知摘要（用于 SSE 推送）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeLatestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String createdName;

    private LocalDateTime createdAt;

    private Integer readStatus;

    private LocalDateTime readTime;
}
