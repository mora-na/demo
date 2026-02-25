package com.example.demo.extension.config;

import com.example.demo.common.config.ConfigBinding;
import com.example.demo.common.config.ConfigField;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 动态接口常量配置。
 */
@ConfigBinding(group = "dynamic.api", hotUpdate = true)
@Data
@Component
@ConfigurationProperties(prefix = "dynamic.api.constants")
public class DynamicApiConstants {

    private Controller controller = new Controller();
    private Message message = new Message();
    private Http http = new Http();
    private Execute execute = new Execute();

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;
        public static final int DEFAULT_SERVICE_UNAVAILABLE_CODE = 503;
        public static final int DEFAULT_RATE_LIMIT_CODE = 429;
        public static final int DEFAULT_REJECTED_CODE = 429;

        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
        private int serviceUnavailableCode = DEFAULT_SERVICE_UNAVAILABLE_CODE;
        private int rateLimitCode = DEFAULT_RATE_LIMIT_CODE;
        private int rejectedCode = DEFAULT_REJECTED_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_NOT_FOUND = "dynamic.api.not.found";
        public static final String DEFAULT_GLOBAL_DISABLED = "dynamic.api.global.disabled";
        public static final String DEFAULT_PATH_INVALID = "dynamic.api.path.invalid";
        public static final String DEFAULT_METHOD_INVALID = "dynamic.api.method.invalid";
        public static final String DEFAULT_TYPE_INVALID = "dynamic.api.type.invalid";
        public static final String DEFAULT_CONFIG_INVALID = "dynamic.api.config.invalid";
        public static final String DEFAULT_BEAN_INVALID = "dynamic.api.bean.invalid";
        public static final String DEFAULT_SQL_INVALID = "dynamic.api.sql.invalid";
        public static final String DEFAULT_HTTP_INVALID = "dynamic.api.http.invalid";
        public static final String DEFAULT_CREATE_FAILED = "dynamic.api.create.failed";
        public static final String DEFAULT_UPDATE_FAILED = "dynamic.api.update.failed";
        public static final String DEFAULT_DELETE_FAILED = "dynamic.api.delete.failed";
        public static final String DEFAULT_STATUS_UPDATE_FAILED = "dynamic.api.status.update.failed";
        public static final String DEFAULT_EXECUTE_FAILED = "dynamic.api.execute.failed";
        public static final String DEFAULT_TIMEOUT = "dynamic.api.timeout";
        public static final String DEFAULT_REJECTED = "dynamic.api.rejected";
        public static final String DEFAULT_CIRCUIT_OPEN = "dynamic.api.circuit.open";
        public static final String DEFAULT_RESPONSE_TOO_LARGE = "dynamic.api.response.too.large";

        private String notFound = DEFAULT_NOT_FOUND;
        private String globalDisabled = DEFAULT_GLOBAL_DISABLED;
        private String pathInvalid = DEFAULT_PATH_INVALID;
        private String methodInvalid = DEFAULT_METHOD_INVALID;
        private String typeInvalid = DEFAULT_TYPE_INVALID;
        private String configInvalid = DEFAULT_CONFIG_INVALID;
        private String beanInvalid = DEFAULT_BEAN_INVALID;
        private String sqlInvalid = DEFAULT_SQL_INVALID;
        private String httpInvalid = DEFAULT_HTTP_INVALID;
        private String createFailed = DEFAULT_CREATE_FAILED;
        private String updateFailed = DEFAULT_UPDATE_FAILED;
        private String deleteFailed = DEFAULT_DELETE_FAILED;
        private String statusUpdateFailed = DEFAULT_STATUS_UPDATE_FAILED;
        private String executeFailed = DEFAULT_EXECUTE_FAILED;
        private String timeout = DEFAULT_TIMEOUT;
        private String rejected = DEFAULT_REJECTED;
        private String circuitOpen = DEFAULT_CIRCUIT_OPEN;
        private String responseTooLarge = DEFAULT_RESPONSE_TOO_LARGE;
    }

    @Data
    public static class Http {
        public static final String DEFAULT_EXT_PREFIX = "/ext/";
        public static final String DEFAULT_ERROR_PATH = "/error";
        public static final String DEFAULT_ACTUATOR_PREFIX = "/actuator";
        public static final String DEFAULT_REQUEST_ID_HEADER = "X-Request-Id";
        public static final String DEFAULT_TENANT_ID_HEADER = "X-Tenant-Id";
        public static final String DEFAULT_TRACE_ID_HEADER = "X-Trace-Id";
        public static final boolean DEFAULT_BLOCK_UNKNOWN_HOST = true;
        public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;
        public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 50;
        public static final int DEFAULT_IDLE_EVICT_SECONDS = 30;
        public static final List<String> DEFAULT_ALLOWED_SCHEMES = Arrays.asList("http", "https");

        private String extPrefix = DEFAULT_EXT_PREFIX;
        private String errorPath = DEFAULT_ERROR_PATH;
        private String actuatorPrefix = DEFAULT_ACTUATOR_PREFIX;
        private String requestIdHeader = DEFAULT_REQUEST_ID_HEADER;
        private String tenantIdHeader = DEFAULT_TENANT_ID_HEADER;
        private String traceIdHeader = DEFAULT_TRACE_ID_HEADER;
        @ConfigField(hotUpdate = false)
        private int maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTIONS;
        @ConfigField(hotUpdate = false)
        private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        @ConfigField(hotUpdate = false)
        private int idleEvictSeconds = DEFAULT_IDLE_EVICT_SECONDS;
        /**
         * HTTP 转发允许的协议（小写）。
         */
        private List<String> allowedSchemes = new ArrayList<>(DEFAULT_ALLOWED_SCHEMES);
        /**
         * HTTP 转发允许的目标主机（为空表示不限制，支持通配符）。
         */
        private List<String> allowedHosts = new ArrayList<>();
        /**
         * HTTP 转发禁止的目标主机（支持通配符）。
         */
        private List<String> blockedHosts = new ArrayList<>();
        /**
         * HTTP 转发允许的目标 CIDR（为空表示不限制）。
         */
        private List<String> allowedCidrs = new ArrayList<>();
        /**
         * HTTP 转发禁止的目标 CIDR。
         */
        private List<String> blockedCidrs = new ArrayList<>();
        /**
         * 是否禁止访问私有网段/回环地址。
         */
        private boolean blockPrivateNetwork = false;
        /**
         * DNS 解析失败是否阻止访问。
         */
        private boolean blockUnknownHost = DEFAULT_BLOCK_UNKNOWN_HOST;
        @ConfigField(hotUpdate = false)
        private List<String> supportedMethods = new ArrayList<>(
                Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE")
        );
    }

    @Data
    public static class Execute {
        public static final int DEFAULT_LOG_MAX_LENGTH = 2000;
        public static final long DEFAULT_TIMEOUT_MS = 3000L;
        public static final long DEFAULT_MAX_TIMEOUT_MS = 60000L;
        public static final long DEFAULT_CLEANUP_TIMEOUT_MS = 1000L;
        public static final long DEFAULT_MAX_RESPONSE_BYTES = 1024 * 1024L;
        public static final int DEFAULT_SQL_MAX_ROWS = 500;
        public static final int DEFAULT_SQL_FETCH_SIZE = 200;
        public static final int DEFAULT_RUNNING_MAX_ENTRIES = 10000;
        public static final long DEFAULT_RUNNING_EXPIRE_BUFFER_MS = 10000L;
        public static final List<String> DEFAULT_MASKED_KEYS = Arrays.asList(
                "password",
                "pass",
                "pwd",
                "token",
                "accessToken",
                "refreshToken",
                "secret",
                "authorization"
        );
        public static final int DEFAULT_CLEANUP_EXECUTOR_CORE_POOL_SIZE = 1;
        public static final int DEFAULT_CLEANUP_EXECUTOR_MAX_POOL_SIZE = 2;
        public static final int DEFAULT_CLEANUP_EXECUTOR_QUEUE_CAPACITY = 200;
        public static final int DEFAULT_CLEANUP_EXECUTOR_KEEP_ALIVE_SECONDS = 30;
        public static final int DEFAULT_CLEANUP_SCHEDULER_POOL_SIZE = 1;
        public static final String DEFAULT_CLEANUP_EXECUTOR_THREAD_NAME_PREFIX = "ext-cleanup-";
        public static final String DEFAULT_CLEANUP_SCHEDULER_THREAD_NAME_PREFIX = "ext-cleanup-scheduler-";

        private int logMaxLength = DEFAULT_LOG_MAX_LENGTH;
        /**
         * 单接口默认超时（毫秒）。
         */
        private long defaultTimeoutMs = DEFAULT_TIMEOUT_MS;
        /**
         * 全局最大超时（毫秒），用于兜底，<=0 表示不限制。
         */
        private long maxTimeoutMs = DEFAULT_MAX_TIMEOUT_MS;
        /**
         * 清理/终止回调最大执行时间（毫秒）。
         */
        private long cleanupTimeoutMs = DEFAULT_CLEANUP_TIMEOUT_MS;
        /**
         * 最大响应体字节数（HTTP/SQL），<=0 表示不限制。
         */
        private long maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;
        /**
         * SQL 查询最大返回行数，<=0 表示不限制。
         */
        private int sqlMaxRows = DEFAULT_SQL_MAX_ROWS;
        /**
         * SQL 游标抓取大小，<=0 表示不设置。
         */
        private int sqlFetchSize = DEFAULT_SQL_FETCH_SIZE;
        /**
         * 运行中请求缓存上限（SQL/HTTP）。
         */
        @ConfigField(hotUpdate = false)
        private int runningMaxEntries = DEFAULT_RUNNING_MAX_ENTRIES;
        /**
         * 运行中请求缓存过期冗余时间（毫秒）。
         */
        @ConfigField(hotUpdate = false)
        private long runningExpireBufferMs = DEFAULT_RUNNING_EXPIRE_BUFFER_MS;
        /**
         * 日志脱敏字段列表（大小写不敏感）。
         */
        private List<String> maskedKeys = new ArrayList<>(DEFAULT_MASKED_KEYS);
        /**
         * 清理执行器核心线程数。
         */
        @ConfigField(hotUpdate = false)
        private int cleanupExecutorCorePoolSize = DEFAULT_CLEANUP_EXECUTOR_CORE_POOL_SIZE;
        /**
         * 清理执行器最大线程数。
         */
        @ConfigField(hotUpdate = false)
        private int cleanupExecutorMaxPoolSize = DEFAULT_CLEANUP_EXECUTOR_MAX_POOL_SIZE;
        /**
         * 清理执行器队列容量。
         */
        @ConfigField(hotUpdate = false)
        private int cleanupExecutorQueueCapacity = DEFAULT_CLEANUP_EXECUTOR_QUEUE_CAPACITY;
        /**
         * 清理执行器线程保活秒数。
         */
        @ConfigField(hotUpdate = false)
        private int cleanupExecutorKeepAliveSeconds = DEFAULT_CLEANUP_EXECUTOR_KEEP_ALIVE_SECONDS;
        /**
         * 清理调度器线程数。
         */
        @ConfigField(hotUpdate = false)
        private int cleanupSchedulerPoolSize = DEFAULT_CLEANUP_SCHEDULER_POOL_SIZE;
        /**
         * 清理执行器线程名前缀。
         */
        @ConfigField(hotUpdate = false)
        private String cleanupExecutorThreadNamePrefix = DEFAULT_CLEANUP_EXECUTOR_THREAD_NAME_PREFIX;
        /**
         * 清理调度器线程名前缀。
         */
        @ConfigField(hotUpdate = false)
        private String cleanupSchedulerThreadNamePrefix = DEFAULT_CLEANUP_SCHEDULER_THREAD_NAME_PREFIX;
    }
}
