package com.example.demo.job.service.impl;

import com.example.demo.job.config.JobConstants;
import com.example.demo.job.entity.SysJob;
import com.example.demo.job.model.JobMisfirePolicy;
import com.example.demo.job.service.JobSchedulerService;
import com.example.demo.job.support.ConcurrentQuartzJob;
import com.example.demo.job.support.DisallowConcurrentQuartzJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

/**
 * Quartz 调度器服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerServiceImpl implements JobSchedulerService {

    private final Scheduler scheduler;
    private final JobConstants jobConstants;

    @Override
    public void syncJob(SysJob job) {
        if (job == null || job.getId() == null) {
            return;
        }
        JobKey jobKey = buildJobKey(job);
        TriggerKey triggerKey = buildTriggerKey(job);
        try {
            JobDetail jobDetail = buildJobDetail(job, jobKey);
            if (scheduler.checkExists(jobKey)) {
                scheduler.addJob(jobDetail, true, true);
            } else {
                scheduler.addJob(jobDetail, true);
            }

            if (jobConstants.getStatus().getJobEnabled() == normalizeStatus(job.getStatus())) {
                CronTrigger trigger = buildTrigger(job, triggerKey);
                if (scheduler.checkExists(triggerKey)) {
                    scheduler.rescheduleJob(triggerKey, trigger);
                } else {
                    scheduler.scheduleJob(trigger);
                }
            } else {
                if (scheduler.checkExists(triggerKey)) {
                    scheduler.unscheduleJob(triggerKey);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to sync quartz job: {}", job.getId(), ex);
        }
    }

    @Override
    public void deleteJob(SysJob job) {
        if (job == null || job.getId() == null) {
            return;
        }
        try {
            scheduler.deleteJob(buildJobKey(job));
        } catch (SchedulerException ex) {
            log.error("Failed to delete quartz job: {}", job.getId(), ex);
        }
    }

    @Override
    public void runOnce(SysJob job) {
        if (job == null || job.getId() == null) {
            return;
        }
        try {
            scheduler.triggerJob(buildJobKey(job));
        } catch (SchedulerException ex) {
            log.error("Failed to trigger quartz job: {}", job.getId(), ex);
        }
    }

    @Override
    public LocalDateTime getNextFireTime(SysJob job) {
        if (job == null || job.getId() == null) {
            return null;
        }
        try {
            Trigger trigger = scheduler.getTrigger(buildTriggerKey(job));
            if (trigger == null) {
                return null;
            }
            Date next = trigger.getNextFireTime();
            if (next == null) {
                return null;
            }
            return LocalDateTime.ofInstant(next.toInstant(), ZoneId.systemDefault());
        } catch (SchedulerException ex) {
            log.warn("Failed to load next fire time for job {}", job.getId(), ex);
            return null;
        }
    }

    private JobDetail buildJobDetail(SysJob job, JobKey jobKey) {
        JobDataMap map = new JobDataMap();
        map.put(jobConstants.getDataMap().getJobIdKey(), job.getId());
        map.put(jobConstants.getDataMap().getJobNameKey(), job.getName());
        map.put(jobConstants.getDataMap().getHandlerNameKey(), job.getHandlerName());
        map.put(jobConstants.getDataMap().getCronExpressionKey(), job.getCronExpression());
        map.put(jobConstants.getDataMap().getParamsKey(), StringUtils.defaultString(job.getParams()));

        Class<? extends Job> jobClass = resolveJobClass(job.getAllowConcurrent());
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .usingJobData(map)
                .storeDurably(true)
                .build();
    }

    private CronTrigger buildTrigger(SysJob job, TriggerKey triggerKey) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        String policy = normalizeMisfirePolicy(job.getMisfirePolicy());
        if (JobMisfirePolicy.IGNORE_MISFIRE.equals(policy)) {
            scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        } else if (JobMisfirePolicy.FIRE_AND_PROCEED.equals(policy)) {
            scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else if (JobMisfirePolicy.DO_NOTHING.equals(policy)) {
            scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(buildJobKey(job))
                .withSchedule(scheduleBuilder)
                .build();
    }

    private JobKey buildJobKey(SysJob job) {
        return JobKey.jobKey(jobConstants.getScheduler().getJobKeyPrefix() + job.getId(),
                jobConstants.getScheduler().getJobGroup());
    }

    private TriggerKey buildTriggerKey(SysJob job) {
        return TriggerKey.triggerKey(jobConstants.getScheduler().getTriggerKeyPrefix() + job.getId(),
                jobConstants.getScheduler().getTriggerGroup());
    }

    private int normalizeStatus(Integer status) {
        return status == null ? jobConstants.getStatus().getJobEnabled() : status;
    }

    private String normalizeMisfirePolicy(String policy) {
        if (policy == null) {
            return JobMisfirePolicy.DEFAULT;
        }
        return policy.trim().toUpperCase(Locale.ROOT);
    }

    private Class<? extends Job> resolveJobClass(Integer allowConcurrent) {
        if (allowConcurrent != null && allowConcurrent == jobConstants.getConcurrent().getDisallow()) {
            return DisallowConcurrentQuartzJob.class;
        }
        return ConcurrentQuartzJob.class;
    }
}
