package com.example.demo.job.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.service.SysJobLogService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
        JobDataMap dataMap = context.getMergedJobDataMap();
        JobContext jobContext = buildContext(dataMap);
        SysJobLogService logService = SpringContextHolder.getBean(SysJobLogService.class);
        LocalDateTime start = LocalDateTime.now();
        boolean success = false;
        String message = null;
        try {
            JobHandlerRegistry registry = SpringContextHolder.getBean(JobHandlerRegistry.class);
            JobHandler handler = registry == null ? null : registry.getHandler(jobContext.getHandlerName());
            if (handler == null) {
                message = "handler not found";
                return;
            }
            handler.execute(jobContext);
            success = true;
        } catch (Exception ex) {
            message = ex.getMessage();
            throw new JobExecutionException(ex);
        } finally {
            if (logService != null) {
                SysJobLog log = new SysJobLog();
                log.setJobId(jobContext.getJobId());
                log.setJobName(jobContext.getJobName());
                log.setHandlerName(jobContext.getHandlerName());
                log.setStatus(success ? SysJobLog.STATUS_SUCCESS : SysJobLog.STATUS_FAILED);
                log.setMessage(trimMessage(message));
                log.setStartTime(start);
                LocalDateTime end = LocalDateTime.now();
                log.setEndTime(end);
                log.setDurationMs(java.time.Duration.between(start, end).toMillis());
                logService.save(log);
            }
        }
    }

    private JobContext buildContext(JobDataMap dataMap) {
        JobContext context = new JobContext();
        context.setJobId(dataMap.getLongValue("jobId"));
        context.setJobName(dataMap.getString("jobName"));
        context.setHandlerName(dataMap.getString("handlerName"));
        context.setCronExpression(dataMap.getString("cronExpression"));
        context.setParams(dataMap.getString("params"));
        return context;
    }

    private String trimMessage(String message) {
        if (message == null) {
            return null;
        }
        String value = message.trim();
        if (value.length() <= 500) {
            return value;
        }
        return value.substring(0, 500);
    }
}
