package com.example.demo.job.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Cron preview request.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/23
 */
@Data
public class JobCronPreviewRequest {

    @NotBlank
    private String cronExpression;
}
