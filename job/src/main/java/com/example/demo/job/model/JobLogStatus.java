package com.example.demo.job.model;

/**
 * 定时任务执行记录状态。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
public final class JobLogStatus {

    /**
     * 执行中。
     */
    public static final int RUNNING = 0;

    /**
     * 执行成功。
     */
    public static final int SUCCESS = 1;

    /**
     * 执行失败。
     */
    public static final int FAILED = 2;

    private JobLogStatus() {
    }
}
