package com.example.demo.job.dto;

import lombok.Data;

import java.util.List;

/**
 * Cron preview response.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/23
 */
@Data
public class JobCronPreviewVO {

    private String cronExpression;
    private String timeZone;
    private List<String> nextFireTimes;
}
