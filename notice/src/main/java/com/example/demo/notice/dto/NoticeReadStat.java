package com.example.demo.notice.dto;

import lombok.Data;

/**
 * 通知阅读统计结果。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class NoticeReadStat {

    private Long noticeId;

    private Long totalCount;

    private Long readCount;
}
