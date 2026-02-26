package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SSE 分发通知摘要。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeStreamDispatchNotice implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String createdName;
    private LocalDateTime createdAt;
}
