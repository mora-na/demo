package com.example.demo.job.config;

import com.example.demo.common.config.ConfigBinding;
import com.example.demo.common.config.ConfigField;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Job 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@ConfigBinding(group = "job")
@Data
@Component
@ConfigurationProperties(prefix = "job.constants")
public class JobConstants {

    private Controller controller = new Controller();
    private Message message = new Message();
    private Page page = new Page();
    private Status status = new Status();
    private Concurrent concurrent = new Concurrent();
    private Scheduler scheduler = new Scheduler();
    private DataMap dataMap = new DataMap();
    @ConfigField(seed = true, hotUpdate = true)
    private Execution execution = new Execution();
    private HandlerDemo handlerDemo = new HandlerDemo();
    @ConfigField(seed = true, hotUpdate = true)
    private LogCollect logCollect = new LogCollect();
    private Appender appender = new Appender();

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        /**
         * 参数或业务校验失败错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 资源不存在错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 服务执行失败错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_JOB_NOT_FOUND = "job.not.found";
        public static final String DEFAULT_JOB_LOG_NOT_FOUND = "job.log.not.found";
        public static final String DEFAULT_JOB_CRON_INVALID = "job.cron.invalid";
        public static final String DEFAULT_JOB_HANDLER_INVALID = "job.handler.invalid";
        public static final String DEFAULT_JOB_MISFIRE_INVALID = "job.misfire.invalid";
        public static final String DEFAULT_JOB_STATUS_INVALID = "job.status.invalid";
        public static final String DEFAULT_JOB_CONCURRENT_INVALID = "job.concurrent.invalid";
        public static final String DEFAULT_JOB_LOG_COLLECT_LEVEL_INVALID = "job.log.collect.level.invalid";
        public static final String DEFAULT_JOB_CREATE_FAILED = "job.create.failed";
        public static final String DEFAULT_JOB_UPDATE_FAILED = "job.update.failed";
        public static final String DEFAULT_JOB_DELETE_FAILED = "job.delete.failed";
        public static final String DEFAULT_JOB_STATUS_UPDATE_FAILED = "job.status.update.failed";
        public static final String DEFAULT_JOB_RUN_FAILED = "job.run.failed";

        private String jobNotFound = DEFAULT_JOB_NOT_FOUND;
        private String jobLogNotFound = DEFAULT_JOB_LOG_NOT_FOUND;
        private String jobCronInvalid = DEFAULT_JOB_CRON_INVALID;
        private String jobHandlerInvalid = DEFAULT_JOB_HANDLER_INVALID;
        private String jobMisfireInvalid = DEFAULT_JOB_MISFIRE_INVALID;
        private String jobStatusInvalid = DEFAULT_JOB_STATUS_INVALID;
        private String jobConcurrentInvalid = DEFAULT_JOB_CONCURRENT_INVALID;
        private String jobLogCollectLevelInvalid = DEFAULT_JOB_LOG_COLLECT_LEVEL_INVALID;
        private String jobCreateFailed = DEFAULT_JOB_CREATE_FAILED;
        private String jobUpdateFailed = DEFAULT_JOB_UPDATE_FAILED;
        private String jobDeleteFailed = DEFAULT_JOB_DELETE_FAILED;
        private String jobStatusUpdateFailed = DEFAULT_JOB_STATUS_UPDATE_FAILED;
        private String jobRunFailed = DEFAULT_JOB_RUN_FAILED;
    }

    @Data
    public static class Page {
        public static final long DEFAULT_PAGE_NUM = 1L;
        public static final long DEFAULT_PAGE_SIZE = 10L;

        /**
         * 分页查询默认页码。
         */
        private long defaultPageNum = DEFAULT_PAGE_NUM;
        /**
         * 分页查询默认页大小。
         */
        private long defaultPageSize = DEFAULT_PAGE_SIZE;
    }

    @Data
    public static class Status {
        public static final int DEFAULT_JOB_ENABLED = 1;
        public static final int DEFAULT_JOB_DISABLED = 0;
        public static final int DEFAULT_LOG_SUCCESS = 1;
        public static final int DEFAULT_LOG_FAILED = 0;

        /**
         * 任务启用状态值。
         */
        private int jobEnabled = DEFAULT_JOB_ENABLED;
        /**
         * 任务禁用状态值。
         */
        private int jobDisabled = DEFAULT_JOB_DISABLED;
        /**
         * 日志执行成功状态值。
         */
        private int logSuccess = DEFAULT_LOG_SUCCESS;
        /**
         * 日志执行失败状态值。
         */
        private int logFailed = DEFAULT_LOG_FAILED;
    }

    @Data
    public static class Concurrent {
        public static final int DEFAULT_ALLOW = 1;
        public static final int DEFAULT_DISALLOW = 0;

        /**
         * 允许并发执行标记值。
         */
        private int allow = DEFAULT_ALLOW;
        /**
         * 禁止并发执行标记值。
         */
        private int disallow = DEFAULT_DISALLOW;
    }

    @Data
    public static class Scheduler {
        public static final String DEFAULT_JOB_GROUP = "SYS_JOB";
        public static final String DEFAULT_TRIGGER_GROUP = "SYS_JOB_TRIGGER";
        public static final String DEFAULT_JOB_KEY_PREFIX = "JOB_";
        public static final String DEFAULT_TRIGGER_KEY_PREFIX = "TRIGGER_";

        private String jobGroup = DEFAULT_JOB_GROUP;
        private String triggerGroup = DEFAULT_TRIGGER_GROUP;
        private String jobKeyPrefix = DEFAULT_JOB_KEY_PREFIX;
        private String triggerKeyPrefix = DEFAULT_TRIGGER_KEY_PREFIX;
    }

    @Data
    public static class DataMap {
        public static final String DEFAULT_JOB_ID_KEY = "jobId";
        public static final String DEFAULT_JOB_NAME_KEY = "jobName";
        public static final String DEFAULT_HANDLER_NAME_KEY = "handlerName";
        public static final String DEFAULT_CRON_EXPRESSION_KEY = "cronExpression";
        public static final String DEFAULT_PARAMS_KEY = "params";
        public static final String DEFAULT_LOG_COLLECT_LEVEL_KEY = "logCollectLevel";

        private String jobIdKey = DEFAULT_JOB_ID_KEY;
        private String jobNameKey = DEFAULT_JOB_NAME_KEY;
        private String handlerNameKey = DEFAULT_HANDLER_NAME_KEY;
        private String cronExpressionKey = DEFAULT_CRON_EXPRESSION_KEY;
        private String paramsKey = DEFAULT_PARAMS_KEY;
        private String logCollectLevelKey = DEFAULT_LOG_COLLECT_LEVEL_KEY;
    }

    @Data
    public static class Execution {
        public static final String DEFAULT_EXECUTE_START_PREFIX = "开始执行: ";
        public static final String DEFAULT_PARAMS_PREFIX = "参数: ";
        public static final String DEFAULT_HANDLER_NOT_FOUND_MESSAGE = "handler not found";
        public static final String DEFAULT_HANDLER_NOT_FOUND_LOG_PREFIX = "处理器不存在: ";
        public static final String DEFAULT_EXECUTE_SUCCESS_LOG = "执行成功";
        public static final String DEFAULT_EXECUTE_ERROR_PREFIX = "执行异常: ";
        public static final String DEFAULT_LOG_MERGE_SEPARATOR = "\n----\n";
        public static final int DEFAULT_MESSAGE_MAX_LENGTH = 500;
        public static final int DEFAULT_LOG_DETAIL_MAX_LENGTH = 8000;

        private String executeStartPrefix = DEFAULT_EXECUTE_START_PREFIX;
        private String paramsPrefix = DEFAULT_PARAMS_PREFIX;
        private String handlerNotFoundMessage = DEFAULT_HANDLER_NOT_FOUND_MESSAGE;
        private String handlerNotFoundLogPrefix = DEFAULT_HANDLER_NOT_FOUND_LOG_PREFIX;
        private String executeSuccessLog = DEFAULT_EXECUTE_SUCCESS_LOG;
        private String executeErrorPrefix = DEFAULT_EXECUTE_ERROR_PREFIX;
        private String logMergeSeparator = DEFAULT_LOG_MERGE_SEPARATOR;
        private int messageMaxLength = DEFAULT_MESSAGE_MAX_LENGTH;
        private int logDetailMaxLength = DEFAULT_LOG_DETAIL_MAX_LENGTH;
    }

    @Data
    public static class HandlerDemo {
        public static final String DEFAULT_MANUAL_LOG_START = "手动记录定时任务日志";
        public static final String DEFAULT_MANUAL_LOG_END = "手动记录日志任务结束";
        public static final String DEFAULT_NEW_THREAD_LOG = "new Thread 未显式透传也可收集日志";
        public static final String DEFAULT_ASYNC_THREAD_LOG = "异步线程日志手动记录";
        public static final String DEFAULT_PLAIN_THREAD_NAME = "job-log-plain";
        public static final String DEFAULT_ASYNC_THREAD_NAME = "job-log-demo";
        public static final int DEFAULT_RAW_EXECUTOR_POOL_SIZE = 1;
        public static final int DEFAULT_WRAPPED_EXECUTOR_POOL_SIZE = 1;
        public static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 100L;

        private String manualLogStart = DEFAULT_MANUAL_LOG_START;
        private String manualLogEnd = DEFAULT_MANUAL_LOG_END;
        private String newThreadLog = DEFAULT_NEW_THREAD_LOG;
        private String asyncThreadLog = DEFAULT_ASYNC_THREAD_LOG;
        private String plainThreadName = DEFAULT_PLAIN_THREAD_NAME;
        private String asyncThreadName = DEFAULT_ASYNC_THREAD_NAME;
        private int rawExecutorPoolSize = DEFAULT_RAW_EXECUTOR_POOL_SIZE;
        private int wrappedExecutorPoolSize = DEFAULT_WRAPPED_EXECUTOR_POOL_SIZE;
        private long scheduleDelayMillis = DEFAULT_SCHEDULE_DELAY_MILLIS;
    }

    @Data
    public static class LogCollect {
        public static final boolean DEFAULT_ENABLED = true;
        public static final Scope DEFAULT_SCOPE = Scope.MDC;
        public static final String DEFAULT_MIN_LEVEL = "INFO";
        public static final int DEFAULT_MAX_LENGTH = 65536;
        public static final int DEFAULT_MAX_BUFFERS = 2000;
        public static final long DEFAULT_MERGE_DELAY_MILLIS = 3000L;
        public static final long DEFAULT_MAX_HOLD_MILLIS = 60000L;
        public static final boolean DEFAULT_INHERIT_THREAD_CONTEXT = true;
        public static final boolean DEFAULT_AUTO_DEGRADE_ENABLED = true;
        public static final double DEFAULT_DEGRADE_BUFFER_RATIO = 0.9d;
        public static final String DEFAULT_MDC_KEY = "jobLogId";
        public static final String DEFAULT_THREAD_KEY = "jobLogThread";
        public static final String DEFAULT_COLLECTOR_THREAD_NAME = "job-log-collector";
        public static final long DEFAULT_CLEANUP_INITIAL_DELAY_MILLIS = 60000L;
        public static final long DEFAULT_CLEANUP_INTERVAL_MILLIS = 60000L;

        private boolean enabled = DEFAULT_ENABLED;
        private Scope scope = DEFAULT_SCOPE;
        private String minLevel = DEFAULT_MIN_LEVEL;
        private int maxLength = DEFAULT_MAX_LENGTH;
        private int maxBuffers = DEFAULT_MAX_BUFFERS;
        private long mergeDelayMillis = DEFAULT_MERGE_DELAY_MILLIS;
        private long maxHoldMillis = DEFAULT_MAX_HOLD_MILLIS;
        private boolean inheritThreadContext = DEFAULT_INHERIT_THREAD_CONTEXT;
        private boolean autoDegradeEnabled = DEFAULT_AUTO_DEGRADE_ENABLED;
        private double degradeBufferRatio = DEFAULT_DEGRADE_BUFFER_RATIO;
        private String mdcKey = DEFAULT_MDC_KEY;
        private String threadKey = DEFAULT_THREAD_KEY;
        private String collectorThreadName = DEFAULT_COLLECTOR_THREAD_NAME;
        private long cleanupInitialDelayMillis = DEFAULT_CLEANUP_INITIAL_DELAY_MILLIS;
        private long cleanupIntervalMillis = DEFAULT_CLEANUP_INTERVAL_MILLIS;

        public enum Scope {
            MDC,
            THREAD
        }
    }

    @Data
    public static class Appender {
        public static final String DEFAULT_APPENDER_NAME = "JOB_LOG_COLLECTOR";
        public static final String DEFAULT_PATTERN =
                "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %X{traceId} [%thread] %logger{36} - %msg%ex";

        private String appenderName = DEFAULT_APPENDER_NAME;
        /**
         * Logback pattern for collected job logs.
         */
        private String pattern = DEFAULT_PATTERN;
    }
}
