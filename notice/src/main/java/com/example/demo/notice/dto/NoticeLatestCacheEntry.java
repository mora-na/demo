package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SSE 最新通知缓存载体，确保跨存储反序列化可用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeLatestCacheEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<NoticeLatestVO> latest;
}
