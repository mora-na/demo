package com.example.demo.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知接收详情视图。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class NoticeRecipientVO {

    private Long id;

    private Long userId;

    private String userName;

    private String nickName;

    private Long deptId;

    private Integer readStatus;

    private LocalDateTime readTime;
}
