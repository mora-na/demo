package com.example.demo.notice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Notice 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "notice.constants")
public class NoticeConstants {

    private Controller controller = new Controller();
    private Message message = new Message();
    private Page page = new Page();
    private Scope scope = new Scope();
    private Recipient recipient = new Recipient();
    private User user = new User();
    private Common common = new Common();
    private Numeric numeric = new Numeric();
    private Stream stream = new Stream();

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_UNAUTHORIZED_CODE = 401;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        /**
         * 参数非法或业务冲突错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 未登录/未授权错误码。
         */
        private int unauthorizedCode = DEFAULT_UNAUTHORIZED_CODE;
        /**
         * 资源不存在错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 持久化失败等服务错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_NOTICE_SCOPE_INVALID = "notice.scope.invalid";
        public static final String DEFAULT_NOTICE_SCOPE_EMPTY = "notice.scope.empty";
        public static final String DEFAULT_NOTICE_RECIPIENTS_EMPTY = "notice.recipients.empty";
        public static final String DEFAULT_NOTICE_NOT_FOUND = "notice.not.found";
        public static final String DEFAULT_AUTH_PERMISSION_REQUIRED = "auth.permission.required";
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";

        private String noticeScopeInvalid = DEFAULT_NOTICE_SCOPE_INVALID;
        private String noticeScopeEmpty = DEFAULT_NOTICE_SCOPE_EMPTY;
        private String noticeRecipientsEmpty = DEFAULT_NOTICE_RECIPIENTS_EMPTY;
        private String noticeNotFound = DEFAULT_NOTICE_NOT_FOUND;
        private String authPermissionRequired = DEFAULT_AUTH_PERMISSION_REQUIRED;
        private String commonDeleteFailed = DEFAULT_COMMON_DELETE_FAILED;
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
    public static class Scope {
        public static final String DEFAULT_ALL = "ALL";
        public static final String DEFAULT_DEPT = "DEPT";
        public static final String DEFAULT_ROLE = "ROLE";
        public static final String DEFAULT_USER = "USER";
        public static final String DEFAULT_SCOPE_VALUE_SEPARATOR = ",";
        public static final String DEFAULT_EMPTY_SCOPE_TYPE = "";

        /**
         * 全员范围标识。
         */
        private String all = DEFAULT_ALL;
        /**
         * 按部门范围标识。
         */
        private String dept = DEFAULT_DEPT;
        /**
         * 按角色范围标识。
         */
        private String role = DEFAULT_ROLE;
        /**
         * 按用户范围标识。
         */
        private String user = DEFAULT_USER;
        /**
         * scopeIds 序列化分隔符。
         */
        private String scopeValueSeparator = DEFAULT_SCOPE_VALUE_SEPARATOR;
        /**
         * scopeType 为空时的默认值。
         */
        private String emptyScopeType = DEFAULT_EMPTY_SCOPE_TYPE;
    }

    @Data
    public static class Recipient {
        public static final int DEFAULT_UNREAD = 0;
        public static final int DEFAULT_READ = 1;

        /**
         * 未读状态值。
         */
        private int unread = DEFAULT_UNREAD;
        /**
         * 已读状态值。
         */
        private int read = DEFAULT_READ;
    }

    @Data
    public static class User {
        public static final int DEFAULT_ENABLED_STATUS = 1;

        /**
         * 用户启用状态值。
         */
        private int enabledStatus = DEFAULT_ENABLED_STATUS;
    }

    @Data
    public static class Common {
        public static final int DEFAULT_NOT_DELETED_FLAG = 0;

        /**
         * 逻辑未删除标记值。
         */
        private int notDeletedFlag = DEFAULT_NOT_DELETED_FLAG;
    }

    @Data
    public static class Numeric {
        public static final long DEFAULT_ZERO_LONG = 0L;
        public static final int DEFAULT_ZERO_INT = 0;

        private long zeroLong = DEFAULT_ZERO_LONG;
        private int zeroInt = DEFAULT_ZERO_INT;
    }

    @Data
    public static class Stream {
        public static final long DEFAULT_ANONYMOUS_EMITTER_TIMEOUT_MILLIS = 60000L;
        public static final long DEFAULT_EMITTER_TIMEOUT_MILLIS = 600000L;
        public static final long DEFAULT_HEARTBEAT_INTERVAL_MILLIS = 30000L;
        public static final long DEFAULT_HEARTBEAT_TIMEOUT_MILLIS = 90000L;
        public static final int DEFAULT_LATEST_LIMIT = 5;
        public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 2000;
        public static final int DEFAULT_MAX_CONNECTIONS_PER_USER = 5;
        public static final long DEFAULT_LATEST_CACHE_MAX_SIZE = 50000L;
        public static final int DEFAULT_LATEST_CACHE_EXPIRE_MINUTES = 30;
        public static final int DEFAULT_CONNECTION_COUNTER_MAX_SIZE = 100000;
        public static final int DEFAULT_CONNECTION_COUNTER_EXPIRE_MINUTES = 30;
        public static final boolean DEFAULT_AUTO_DEGRADE_ENABLED = true;
        public static final double DEFAULT_DEGRADE_CONNECTION_RATIO = 0.8d;
        public static final double DEFAULT_DEGRADE_CACHE_RATIO = 0.9d;
        public static final String DEFAULT_EVENT_NOTICE_NAME = "notice";
        public static final String DEFAULT_EVENT_INIT_NAME = "init";
        public static final String DEFAULT_EVENT_PING_NAME = "ping";
        public static final String DEFAULT_HEARTBEAT_THREAD_NAME = "notice-sse-heartbeat";
        public static final String DEFAULT_LOG_HEARTBEAT_DISABLED =
                "Notice SSE heartbeat disabled (interval={}ms).";
        public static final String DEFAULT_LOG_PUSH_FAILED =
                "Failed to push notice to user {}, removing emitter.";
        public static final String DEFAULT_LOG_PUSH_UPDATE_FAILED =
                "Failed to push notice update to user {}, removing emitter.";
        public static final String DEFAULT_LOG_INIT_FAILED =
                "Failed to send init payload to user {}, removing emitter.";
        public static final String DEFAULT_LOG_HEARTBEAT_FAILED =
                "Heartbeat failed for user {}, removing emitter: {}";

        /**
         * 匿名用户 SSE 连接超时时间（毫秒）。
         */
        private long anonymousEmitterTimeoutMillis = DEFAULT_ANONYMOUS_EMITTER_TIMEOUT_MILLIS;
        /**
         * 已登录用户 SSE 连接超时时间（毫秒）。
         */
        private long emitterTimeoutMillis = DEFAULT_EMITTER_TIMEOUT_MILLIS;
        /**
         * 心跳间隔（毫秒），<=0 表示禁用心跳。
         */
        private long heartbeatIntervalMillis = DEFAULT_HEARTBEAT_INTERVAL_MILLIS;
        /**
         * 前端断线判定超时（毫秒）。
         */
        private long heartbeatTimeoutMillis = DEFAULT_HEARTBEAT_TIMEOUT_MILLIS;
        /**
         * SSE 最新通知列表缓存长度。
         */
        private int latestLimit = DEFAULT_LATEST_LIMIT;
        /**
         * SSE 总连接上限，<=0 表示不限制。
         */
        private int maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTIONS;
        /**
         * 单用户 SSE 连接上限，<=0 表示不限制。
         */
        private int maxConnectionsPerUser = DEFAULT_MAX_CONNECTIONS_PER_USER;
        /**
         * latestCache 最大用户数，<=0 表示禁用缓存。
         */
        private long latestCacheMaxSize = DEFAULT_LATEST_CACHE_MAX_SIZE;
        /**
         * latestCache 空闲过期时间（分钟）。
         */
        private int latestCacheExpireMinutes = DEFAULT_LATEST_CACHE_EXPIRE_MINUTES;
        /**
         * 用户连接计数缓存最大条数。
         */
        private int connectionCounterMaxSize = DEFAULT_CONNECTION_COUNTER_MAX_SIZE;
        /**
         * 用户连接计数缓存空闲过期时间（分钟）。
         */
        private int connectionCounterExpireMinutes = DEFAULT_CONNECTION_COUNTER_EXPIRE_MINUTES;
        /**
         * 是否启用自动降级（按负载）。
         */
        private boolean autoDegradeEnabled = DEFAULT_AUTO_DEGRADE_ENABLED;
        /**
         * 连接数降级阈值比例（0-1）。
         */
        private double degradeConnectionRatio = DEFAULT_DEGRADE_CONNECTION_RATIO;
        /**
         * 缓存占用降级阈值比例（0-1）。
         */
        private double degradeCacheRatio = DEFAULT_DEGRADE_CACHE_RATIO;
        /**
         * 新通知推送事件名。
         */
        private String eventNoticeName = DEFAULT_EVENT_NOTICE_NAME;
        /**
         * 初始化事件名。
         */
        private String eventInitName = DEFAULT_EVENT_INIT_NAME;
        /**
         * 心跳事件名。
         */
        private String eventPingName = DEFAULT_EVENT_PING_NAME;
        /**
         * SSE 心跳线程名。
         */
        private String heartbeatThreadName = DEFAULT_HEARTBEAT_THREAD_NAME;
        /**
         * 心跳关闭日志模板。
         */
        private String logHeartbeatDisabled = DEFAULT_LOG_HEARTBEAT_DISABLED;
        /**
         * 新通知推送失败日志模板。
         */
        private String logPushFailed = DEFAULT_LOG_PUSH_FAILED;
        /**
         * 未读数推送失败日志模板。
         */
        private String logPushUpdateFailed = DEFAULT_LOG_PUSH_UPDATE_FAILED;
        /**
         * 初始化事件发送失败日志模板。
         */
        private String logInitFailed = DEFAULT_LOG_INIT_FAILED;
        /**
         * 心跳发送失败日志模板。
         */
        private String logHeartbeatFailed = DEFAULT_LOG_HEARTBEAT_FAILED;
    }
}
