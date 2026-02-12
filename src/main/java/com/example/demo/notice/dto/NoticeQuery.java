package com.example.demo.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;

    private String scopeType;
}
