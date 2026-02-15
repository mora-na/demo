package com.example.demo.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的通知视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class NoticeMyVO {

    private Long id;

    private String title;

    private String content;

    private String createdName;

    private LocalDateTime createdAt;

    private Integer readStatus;

    private LocalDateTime readTime;
}
