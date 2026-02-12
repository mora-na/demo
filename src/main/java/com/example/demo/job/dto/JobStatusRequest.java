package com.example.demo.job.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 定时任务状态更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class JobStatusRequest {

    @NotNull
    private Integer status;
}
