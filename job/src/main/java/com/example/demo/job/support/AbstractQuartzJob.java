package com.example.demo.job.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.log.JobLogCollector;
import com.example.demo.job.log.JobLogThreadContext;
import com.example.demo.job.service.SysJobLogService;
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
public abstract class AbstractQuartzJob implements Job {

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
        if (logCollector != null && logCollector.isEnabled()) {
            runId = logCollector.start();
            if (runId != null) {
                MDC.put(logCollector.getMdcKey(), runId);
                MDC.put(logCollector.getThreadKey(), Thread.currentThread().getName());
                JobLogThreadContext.set(runId);
            }
        }
        jobContext.appendLog(constants.getExecution().getExecuteStartPrefix() + start);
        if (jobContext.getParams() != null && !jobContext.getParams().trim().isEmpty()) {
            jobContext.appendLog(constants.getExecution().getParamsPrefix() + jobContext.getParams());
        }
        try {
            JobHandlerRegistry registry = SpringContextHolder.getBean(JobHandlerRegistry.class);
            JobHandler handler = registry == null ? null : registry.getHandler(jobContext.getHandlerName());
            if (handler == null) {
                message = constants.getExecution().getHandlerNotFoundMessage();
                jobContext.appendLog(constants.getExecution().getHandlerNotFoundLogPrefix() + jobContext.getHandlerName());
                return;
            }
            handler.execute(jobContext);
            success = true;
            jobContext.appendLog(constants.getExecution().getExecuteSuccessLog());
        } catch (Exception ex) {
            message = ex.getMessage();
            jobContext.appendLog(constants.getExecution().getExecuteErrorPrefix()
                    + (message == null ? ex.getClass().getSimpleName() : message));
            jobContext.appendLog(buildStackTrace(ex));
            throw new JobExecutionException(ex);
        } finally {
            String manualLog = jobContext.getLogContent();
            if (logCollector != null) {
                MDC.remove(logCollector.getMdcKey());
                MDC.remove(logCollector.getThreadKey());
            }
            JobLogThreadContext.clear();
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
                String merged = logCollector != null
                        ? logCollector.mergeLogs(manualLog, runId == null ? null : logCollector.finish(runId))
                        : mergeLogDetail(manualLog, null, constants.getExecution().getLogMergeSeparator());
                log.setLogDetail(logCollector != null
                        ? merged
                        : trimLogDetail(merged, constants.getExecution().getLogDetailMaxLength()));
                logService.save(log);
                if (logCollector != null && runId != null) {
                    if (logCollector.shouldDelayMerge()) {
                        logCollector.scheduleMerge(runId, log.getId(), manualLog);
                    } else {
                        logCollector.close(runId);
                    }
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
        return context;
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

    private String mergeLogDetail(String manual, String autoLog, String separator) {
        String left = manual == null ? "" : manual.trim();
        String right = autoLog == null ? "" : autoLog.trim();
        if (left.isEmpty()) {
            return right.isEmpty() ? null : right;
        }
        if (right.isEmpty()) {
            return left;
        }
        return left + separator + right;
    }

    private String buildStackTrace(Exception ex) {
        java.io.StringWriter writer = new java.io.StringWriter();
        java.io.PrintWriter printWriter = new java.io.PrintWriter(writer);
        ex.printStackTrace(printWriter);
        return writer.toString();
    }

    private JobConstants resolveConstants() {
        JobConstants constants = SpringContextHolder.getBean(JobConstants.class);
        return constants == null ? new JobConstants() : constants;
    }
}
