package com.example.demo.notice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 系统通知发布请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class NoticePublishRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    @Size(max = 32)
    private String scopeType;

    private List<Long> scopeIds;
}
