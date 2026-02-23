package com.example.demo.job.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.job.api.JobContext;
import com.example.demo.job.api.JobHandler;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.log.JobLogCollector;
import com.example.demo.job.log.JobLogThreadContext;
import com.example.demo.job.model.JobLogDetailPart;
import com.example.demo.job.service.SysJobLogDetailService;
import com.example.demo.job.service.SysJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.MDC;

import java.time.LocalDateTime;

/**
 * Quartz 任务基类，负责桥接任务处理器与日志记录。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {

    private static final String LOG_PREFIX = "[Job-Execution] ";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobConstants constants = resolveConstants();
        JobDataMap dataMap = context.getMergedJobDataMap();
        JobContext jobContext = buildContext(dataMap, constants);
        SysJobLogService logService = SpringContextHolder.getBean(SysJobLogService.class);
        JobLogCollector logCollector = SpringContextHolder.getBean(JobLogCollector.class);
        LocalDateTime start = LocalDateTime.now();
        boolean success = false;
        String message = null;
        String runId = null;
        SysJobLogDetailService detailService = SpringContextHolder.getBean(SysJobLogDetailService.class);
        if (logCollector != null && logCollector.isEnabled()) {
            runId = logCollector.start(jobContext.getLogCollectLevel());
            if (runId != null) {
                MDC.put(logCollector.getMdcKey(), runId);
                MDC.put(logCollector.getThreadKey(), Thread.currentThread().getName());
                JobLogThreadContext.set(runId);
            }
        }
        logExecutionInfo(constants.getExecution().getExecuteStartPrefix() + start);
        if (jobContext.getParams() != null && !jobContext.getParams().trim().isEmpty()) {
            logExecutionInfo(constants.getExecution().getParamsPrefix() + jobContext.getParams());
        }
        try {
            JobHandlerRegistry registry = SpringContextHolder.getBean(JobHandlerRegistry.class);
            JobHandler handler = registry == null ? null : registry.getHandler(jobContext.getHandlerName());
            if (handler == null) {
                message = constants.getExecution().getHandlerNotFoundMessage();
                logExecutionWarn(constants.getExecution().getHandlerNotFoundLogPrefix() + jobContext.getHandlerName());
                return;
            }
            handler.execute(jobContext);
            success = true;
            logExecutionInfo(constants.getExecution().getExecuteSuccessLog());
        } catch (Exception ex) {
            message = ex.getMessage();
            logExecutionError(constants.getExecution().getExecuteErrorPrefix()
                    + (message == null ? ex.getClass().getSimpleName() : message), ex);
            throw new JobExecutionException(ex);
        } finally {
            String autoLog = null;
            if (logCollector != null) {
                MDC.remove(logCollector.getMdcKey());
                MDC.remove(logCollector.getThreadKey());
            }
            JobLogThreadContext.clear();
            if (logCollector != null && runId != null) {
                autoLog = logCollector.finish(runId);
            }
            if (logService != null) {
                SysJobLog log = new SysJobLog();
                log.setJobId(jobContext.getJobId());
                log.setJobName(jobContext.getJobName());
                log.setHandlerName(jobContext.getHandlerName());
                log.setStatus(success ? constants.getStatus().getLogSuccess() : constants.getStatus().getLogFailed());
                log.setMessage(trimMessage(message, constants.getExecution().getMessageMaxLength()));
                log.setStartTime(start);
                LocalDateTime end = LocalDateTime.now();
                log.setEndTime(end);
                log.setDurationMs(java.time.Duration.between(start, end).toMillis());
                if (detailService == null) {
                    log.setLogDetail(trimLogDetail(autoLog, constants.getExecution().getLogDetailMaxLength()));
                }
                logService.save(log);
                if (detailService != null) {
                    if (logCollector != null && runId != null) {
                        if (logCollector.shouldDelayMerge()) {
                            logCollector.scheduleMerge(runId, log.getId());
                        } else {
                            detailService.saveDetail(log.getId(), JobLogDetailPart.AUTO, autoLog);
                            logCollector.close(runId);
                        }
                    }
                } else if (logCollector != null && runId != null) {
                    logCollector.close(runId);
                }
            }
        }
    }

    private JobContext buildContext(JobDataMap dataMap, JobConstants constants) {
        JobContext context = new JobContext();
        context.setJobId(dataMap.getLongValue(constants.getDataMap().getJobIdKey()));
        context.setJobName(dataMap.getString(constants.getDataMap().getJobNameKey()));
        context.setHandlerName(dataMap.getString(constants.getDataMap().getHandlerNameKey()));
        context.setCronExpression(dataMap.getString(constants.getDataMap().getCronExpressionKey()));
        context.setParams(dataMap.getString(constants.getDataMap().getParamsKey()));
        context.setLogCollectLevel(dataMap.getString(constants.getDataMap().getLogCollectLevelKey()));
        return context;
    }

    private void logExecutionInfo(String message) {
        log.info(LOG_PREFIX + message);
    }

    private void logExecutionWarn(String message) {
        log.warn(LOG_PREFIX + message);
    }

    private void logExecutionError(String message, Exception ex) {
        log.error(LOG_PREFIX + message, ex);
    }

    private String trimMessage(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        String value = message.trim();
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String trimLogDetail(String detail, int maxLength) {
        if (detail == null) {
            return null;
        }
        String value = detail.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private JobConstants resolveConstants() {
        JobConstants constants = SpringContextHolder.getBean(JobConstants.class);
        return constants == null ? new JobConstants() : constants;
    }
}
