package com.example.demo.job.support;

/**
 * 定时任务处理器接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface JobHandler {

    void execute(JobContext context) throws Exception;
}
