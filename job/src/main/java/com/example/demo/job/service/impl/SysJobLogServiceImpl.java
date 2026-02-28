package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.job.api.JobContext;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.JobLogQuery;
import com.example.demo.job.dto.JobLogVO;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.mapper.SysJobLogMapper;
import com.example.demo.job.model.JobLogStatus;
import com.example.demo.job.model.JobLogTriggerType;
import com.example.demo.job.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 定时任务执行记录服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    private static final int ERROR_MESSAGE_LIMIT = 500;

    private final JobConstants jobConstants;

    @Override
    public IPage<SysJobLog> selectJobLogsPage(Page<SysJobLog> page, JobLogQuery query) {
        Page<SysJobLog> resolved = page == null
                ? new Page<>(jobConstants.getPage().getDefaultPageNum(), jobConstants.getPage().getDefaultPageSize())
                : page;
        return this.page(resolved, buildQuery(query));
    }

    @Override
    public SysJobLog createStartLog(JobExecutionContext context, JobContext jobContext, LocalDateTime startTime) {
        if (context == null || jobContext == null || jobContext.getJobId() == null) {
            return null;
        }
        JobDataMap dataMap = context.getMergedJobDataMap();
        SysJobLog log = new SysJobLog();
        log.setJobId(jobContext.getJobId());
        log.setJobName(jobContext.getJobName());
        log.setHandlerName(jobContext.getHandlerName());
        log.setCronExpression(jobContext.getCronExpression());
        log.setParams(jobContext.getParams());
        log.setTriggerType(resolveTriggerType(dataMap));
        log.setTriggerUserId(resolveTriggerUserId(dataMap));
        log.setTriggerUserName(resolveTriggerUserName(dataMap));
        log.setFireTime(toLocalDateTime(context.getFireTime()));
        log.setScheduledFireTime(toLocalDateTime(context.getScheduledFireTime()));
        log.setStartTime(startTime == null ? LocalDateTime.now() : startTime);
        log.setStatus(JobLogStatus.RUNNING);
        log.setSchedulerInstance(resolveSchedulerInstance(context));
        log.setFireInstanceId(context.getFireInstanceId());
        log.setCreatedAt(log.getStartTime());
        log.setUpdatedAt(log.getStartTime());
        if (!save(log)) {
            return null;
        }
        return log;
    }

    @Override
    public void markSuccess(SysJobLog log, LocalDateTime endTime) {
        if (log == null || log.getId() == null) {
            return;
        }
        LocalDateTime end = endTime == null ? LocalDateTime.now() : endTime;
        Long durationMs = resolveDuration(log.getStartTime(), end);
        SysJobLog update = new SysJobLog();
        update.setId(log.getId());
        update.setStatus(JobLogStatus.SUCCESS);
        update.setEndTime(end);
        update.setDurationMs(durationMs);
        update.setUpdatedAt(end);
        updateById(update);
    }

    @Override
    public void markFailure(SysJobLog log, LocalDateTime endTime, String errorMessage, String errorStacktrace) {
        if (log == null || log.getId() == null) {
            return;
        }
        LocalDateTime end = endTime == null ? LocalDateTime.now() : endTime;
        Long durationMs = resolveDuration(log.getStartTime(), end);
        SysJobLog update = new SysJobLog();
        update.setId(log.getId());
        update.setStatus(JobLogStatus.FAILED);
        update.setEndTime(end);
        update.setDurationMs(durationMs);
        update.setErrorMessage(trimErrorMessage(errorMessage));
        update.setErrorStacktrace(StringUtils.trimToNull(errorStacktrace));
        update.setUpdatedAt(end);
        updateById(update);
    }

    @Override
    public JobLogVO toView(SysJobLog log) {
        if (log == null) {
            return null;
        }
        JobLogVO view = new JobLogVO();
        view.setId(log.getId());
        view.setJobId(log.getJobId());
        view.setJobName(log.getJobName());
        view.setHandlerName(log.getHandlerName());
        view.setCronExpression(log.getCronExpression());
        view.setParams(log.getParams());
        view.setTriggerType(log.getTriggerType());
        view.setTriggerUserId(log.getTriggerUserId());
        view.setTriggerUserName(log.getTriggerUserName());
        view.setFireTime(log.getFireTime());
        view.setScheduledFireTime(log.getScheduledFireTime());
        view.setStartTime(log.getStartTime());
        view.setEndTime(log.getEndTime());
        view.setDurationMs(log.getDurationMs());
        view.setStatus(log.getStatus());
        view.setErrorMessage(log.getErrorMessage());
        view.setErrorStacktrace(log.getErrorStacktrace());
        view.setSchedulerInstance(log.getSchedulerInstance());
        view.setFireInstanceId(log.getFireInstanceId());
        return view;
    }

    private LambdaQueryWrapper<SysJobLog> buildQuery(JobLogQuery query) {
        if (query == null) {
            return Wrappers.lambdaQuery(SysJobLog.class)
                    .orderByDesc(SysJobLog::getStartTime)
                    .orderByDesc(SysJobLog::getId);
        }
        String triggerType = StringUtils.trimToNull(query.getTriggerType());
        return Wrappers.lambdaQuery(SysJobLog.class)
                .eq(query.getJobId() != null, SysJobLog::getJobId, query.getJobId())
                .eq(query.getStatus() != null, SysJobLog::getStatus, query.getStatus())
                .eq(triggerType != null, SysJobLog::getTriggerType, normalizeTriggerType(triggerType))
                .ge(query.getStartTimeFrom() != null, SysJobLog::getStartTime, query.getStartTimeFrom())
                .le(query.getStartTimeTo() != null, SysJobLog::getStartTime, query.getStartTimeTo())
                .orderByDesc(SysJobLog::getStartTime)
                .orderByDesc(SysJobLog::getId);
    }

    private String resolveTriggerType(JobDataMap dataMap) {
        String type = resolveString(dataMap, jobConstants.getDataMap().getTriggerTypeKey());
        if (StringUtils.isBlank(type)) {
            return JobLogTriggerType.SCHEDULED;
        }
        return normalizeTriggerType(type);
    }

    private String normalizeTriggerType(String type) {
        return type == null ? JobLogTriggerType.SCHEDULED : type.trim().toUpperCase();
    }

    private Long resolveTriggerUserId(JobDataMap dataMap) {
        Object value = dataMap == null ? null : dataMap.get(jobConstants.getDataMap().getTriggerUserIdKey());
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String raw = ((String) value).trim();
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private String resolveTriggerUserName(JobDataMap dataMap) {
        return StringUtils.trimToNull(resolveString(dataMap, jobConstants.getDataMap().getTriggerUserNameKey()));
    }

    private String resolveString(JobDataMap dataMap, String key) {
        if (dataMap == null || StringUtils.isBlank(key)) {
            return null;
        }
        Object value = dataMap.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private String resolveSchedulerInstance(JobExecutionContext context) {
        if (context == null || context.getScheduler() == null) {
            return null;
        }
        try {
            return context.getScheduler().getSchedulerInstanceId();
        } catch (SchedulerException ex) {
            return null;
        }
    }

    private Long resolveDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        long duration = Duration.between(start, end).toMillis();
        return Math.max(duration, 0L);
    }

    private String trimErrorMessage(String message) {
        String trimmed = StringUtils.trimToNull(message);
        if (trimmed == null) {
            return null;
        }
        if (trimmed.length() <= ERROR_MESSAGE_LIMIT) {
            return trimmed;
        }
        return StringUtils.abbreviate(trimmed, ERROR_MESSAGE_LIMIT);
    }
}
