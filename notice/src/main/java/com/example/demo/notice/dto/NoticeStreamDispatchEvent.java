package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SSE 跨节点分发事件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeStreamDispatchEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private NoticeStreamDispatchType type;
    private List<Long> userIds;
    private Map<Long, Long> unreadCounts;
    private List<Long> removedNoticeIds;
    private NoticeStreamDispatchNotice notice;
}
