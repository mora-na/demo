package com.example.demo.job.support;

import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.JobQuery;
import com.example.demo.job.entity.SysJob;
import com.example.demo.job.service.JobSchedulerService;
import com.example.demo.job.service.SysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
    private final JobParamValidator jobParamValidator;
    private final JobConstants jobConstants;

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public void loadJobs() {
        JobQuery query = new JobQuery(null, null, jobConstants.getStatus().getJobEnabled());
        List<SysJob> jobs = jobService.selectJobs(query);
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        int loaded = 0;
        for (SysJob job : jobs) {
            if (job == null) {
                continue;
            }
            if (!jobParamValidator.isValidCron(job.getCronExpression())) {
                log.warn("Skip job {}: invalid cron '{}'", job.getId(), job.getCronExpression());
                continue;
            }
            if (!jobParamValidator.isValidHandler(job.getHandlerName())) {
                log.warn("Skip job {}: handler '{}' not found", job.getId(), job.getHandlerName());
                continue;
            }
            schedulerService.syncJob(job);
            loaded++;
        }
        log.info("Loaded {} jobs into Quartz", loaded);
    }
}
