package com.example.demo.job.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.job.api.JobContext;
import com.example.demo.job.api.JobHandler;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.service.SysJobLogService;
import com.logcollect.api.annotation.LogCollect;
import com.logcollect.api.model.LogCollectContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
@Slf4j
public abstract class AbstractQuartzJob implements Job {

    private static final String LOG_PREFIX = "[Job-Execution] ";
    private static final String EXECUTE_START_PREFIX = "开始执行: ";
    private static final String PARAMS_PREFIX = "参数: ";
    private static final String HANDLER_NOT_FOUND_LOG_PREFIX = "处理器不存在: ";
    private static final String EXECUTE_SUCCESS_LOG = "执行成功";
    private static final String EXECUTE_ERROR_PREFIX = "执行异常: ";

    @Override
    @LogCollect(handler = QuartzLogCollectHandler.class, level = "DEBUG")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobConstants constants = resolveConstants();
        JobDataMap dataMap = context.getMergedJobDataMap();
        JobContext jobContext = buildContext(dataMap, constants);
        LocalDateTime start = LocalDateTime.now();
        SysJobLogService logService = SpringContextHolder.getBean(SysJobLogService.class);
        SysJobLog logRecord = logService == null ? null : logService.createStartLog(context, jobContext, start);
        if (logRecord != null) {
            LogCollectContext.setCurrentBusinessId(logRecord.getId());
            jobContext.setExecutionLogId(logRecord.getId());
        }
        logExecutionInfo(EXECUTE_START_PREFIX + start);
        if (jobContext.getParams() != null && !jobContext.getParams().trim().isEmpty()) {
            logExecutionInfo(PARAMS_PREFIX + jobContext.getParams());
        }
        try {
            JobHandlerRegistry registry = SpringContextHolder.getBean(JobHandlerRegistry.class);
            JobHandler handler = registry == null ? null : registry.getHandler(jobContext.getHandlerName());
            if (handler == null) {
                logExecutionWarn(HANDLER_NOT_FOUND_LOG_PREFIX + jobContext.getHandlerName());
                if (logService != null) {
                    logService.markFailure(logRecord, LocalDateTime.now(),
                            HANDLER_NOT_FOUND_LOG_PREFIX + jobContext.getHandlerName(), null);
                }
                return;
            }
            handler.execute(jobContext);
            logExecutionInfo(EXECUTE_SUCCESS_LOG);
            if (logService != null) {
                logService.markSuccess(logRecord, LocalDateTime.now());
            }
        } catch (Exception ex) {
            String message = ex.getMessage();
            logExecutionError(EXECUTE_ERROR_PREFIX
                    + (message == null ? ex.getClass().getSimpleName() : message), ex);
            if (logService != null) {
                String errorMessage = message == null ? ex.getClass().getSimpleName() : message;
                logService.markFailure(logRecord, LocalDateTime.now(), errorMessage, ExceptionUtils.getStackTrace(ex));
            }
            throw new JobExecutionException(ex);
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

    private void logExecutionInfo(String message) {
        log.info(LOG_PREFIX + "{}", message);
    }

    private void logExecutionWarn(String message) {
        log.warn(LOG_PREFIX + "{}", message);
    }

    private void logExecutionError(String message, Exception ex) {
        log.error(LOG_PREFIX + "{}", message, ex);
    }

    private JobConstants resolveConstants() {
        JobConstants constants = SpringContextHolder.getBean(JobConstants.class);
        return constants == null ? new JobConstants() : constants;
    }
}
