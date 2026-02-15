package com.example.demo.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知列表视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class NoticeVO {

    private Long id;

    private String title;

    private String content;

    private String scopeType;

    private String scopeValue;

    private Long createdBy;

    private String createdName;

    private LocalDateTime createdAt;

    private Long totalCount;

    private Long readCount;
}
