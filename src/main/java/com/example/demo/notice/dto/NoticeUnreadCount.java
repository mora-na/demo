package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户未读通知数量聚合结果。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeUnreadCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long unreadCount;
}
