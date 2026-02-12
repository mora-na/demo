package com.example.demo.job.support;

import com.example.demo.job.entity.SysJob;
import com.example.demo.job.service.JobSchedulerService;
import com.example.demo.job.service.SysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动后加载已配置的任务到 Quartz。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobBootstrap {

    private final SysJobService jobService;
    private final JobSchedulerService schedulerService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadJobs() {
        List<SysJob> jobs = jobService.list();
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        for (SysJob job : jobs) {
            schedulerService.syncJob(job);
        }
        log.info("Loaded {} jobs into Quartz", jobs.size());
    }
}
