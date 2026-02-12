package com.example.demo.job.support;

import org.quartz.DisallowConcurrentExecution;

/**
 * 不允许并发执行的 Quartz 任务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@DisallowConcurrentExecution
public class DisallowConcurrentQuartzJob extends AbstractQuartzJob {
}
