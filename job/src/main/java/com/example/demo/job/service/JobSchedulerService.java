package com.example.demo.job.service;

import com.example.demo.job.entity.SysJob;

/**
 * Quartz 调度器封装服务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface JobSchedulerService {

    void syncJob(SysJob job);

    void deleteJob(SysJob job);

    void runOnce(SysJob job, Long triggerUserId, String triggerUserName);

    java.time.LocalDateTime getNextFireTime(SysJob job);
}
