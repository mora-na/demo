package com.example.demo.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Common 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@ConfigBinding(group = "common", hotUpdate = true)
@Data
@Component
@ConfigurationProperties(prefix = "common.constants")
public class CommonConstants {

    private Http http = new Http();
    private Trace trace = new Trace();
    private RateLimit rateLimit = new RateLimit();
    private DuplicateSubmit duplicateSubmit = new DuplicateSubmit();
    private Permission permission = new Permission();
    @ConfigField(hotUpdate = false)
    private Mybatis mybatis = new Mybatis();
    @ConfigField(hotUpdate = false)
    private I18n i18n = new I18n();
    @ConfigField(hotUpdate = false)
    private Mdc mdc = new Mdc();
    @ConfigField(hotUpdate = false)
    private Cache cache = new Cache();
    private ExceptionHandling exceptionHandling = new ExceptionHandling();

    @Data
    public static class Http {
        public static final String DEFAULT_JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
        public static final String DEFAULT_FORWARDED_FOR_HEADER = "X-Forwarded-For";
        public static final String DEFAULT_REAL_IP_HEADER = "X-Real-IP";
        public static final String DEFAULT_MULTIPART_PREFIX = "multipart/";
        public static final String DEFAULT_IDEMPOTENCY_HEADER = "Idempotency-Key";
        public static final boolean DEFAULT_TRUST_FORWARDED_HEADERS = false;

        private String jsonContentType = DEFAULT_JSON_CONTENT_TYPE;
        private String forwardedForHeader = DEFAULT_FORWARDED_FOR_HEADER;
        private String realIpHeader = DEFAULT_REAL_IP_HEADER;
        private String multipartPrefix = DEFAULT_MULTIPART_PREFIX;
        private String idempotencyHeaderDefault = DEFAULT_IDEMPOTENCY_HEADER;
        private boolean trustForwardedHeaders = DEFAULT_TRUST_FORWARDED_HEADERS;
    }

    @Data
    public static class Trace {
        public static final String DEFAULT_MDC_KEY = "traceId";

        private String mdcKey = DEFAULT_MDC_KEY;
    }

    @Data
    public static class RateLimit {
        public static final String DEFAULT_KEY_PREFIX = "rl:";
        public static final int DEFAULT_RESPONSE_STATUS = 429;
        public static final String DEFAULT_MESSAGE_KEY = "common.rate.limit.exceeded";

        private String keyPrefix = DEFAULT_KEY_PREFIX;
        private int responseStatus = DEFAULT_RESPONSE_STATUS;
        private String messageKey = DEFAULT_MESSAGE_KEY;
    }

    @Data
    public static class DuplicateSubmit {
        public static final String DEFAULT_KEY_PREFIX = "dup:";
        public static final int DEFAULT_RESPONSE_STATUS = 409;
        public static final String DEFAULT_MESSAGE_KEY = "common.duplicate.submission";
        public static final String DEFAULT_KEY_IDEMPOTENCY_TAG = "k";
        public static final String DEFAULT_KEY_QUERY_TAG = "q";
        public static final String DEFAULT_KEY_BODY_TAG = "b";

        private String keyPrefix = DEFAULT_KEY_PREFIX;
        private int responseStatus = DEFAULT_RESPONSE_STATUS;
        private String messageKey = DEFAULT_MESSAGE_KEY;
        private String keyIdempotencyTag = DEFAULT_KEY_IDEMPOTENCY_TAG;
        private String keyQueryTag = DEFAULT_KEY_QUERY_TAG;
        private String keyBodyTag = DEFAULT_KEY_BODY_TAG;
    }

    @Data
    public static class Permission {
        public static final String DEFAULT_REQUIRED_MESSAGE_KEY = "auth.permission.required";
        public static final String DEFAULT_DENIED_MESSAGE_KEY = "auth.permission.denied";

        private String requiredMessageKey = DEFAULT_REQUIRED_MESSAGE_KEY;
        private String deniedMessageKey = DEFAULT_DENIED_MESSAGE_KEY;
    }

    @Data
    public static class Mybatis {
        public static final String DEFAULT_DATASOURCE_URL_PROPERTY = "spring.datasource.url";
        public static final String DEFAULT_POSTGRES_TOKEN = ":postgresql:";
        public static final String DEFAULT_MYSQL_TOKEN = ":mysql:";
        public static final String DEFAULT_MARIADB_TOKEN = ":mariadb:";
        public static final String DEFAULT_ORACLE_TOKEN = ":oracle:";

        private String datasourceUrlProperty = DEFAULT_DATASOURCE_URL_PROPERTY;
        private String postgresToken = DEFAULT_POSTGRES_TOKEN;
        private String mysqlToken = DEFAULT_MYSQL_TOKEN;
        private String mariadbToken = DEFAULT_MARIADB_TOKEN;
        private String oracleToken = DEFAULT_ORACLE_TOKEN;
    }

    @Data
    public static class I18n {
        public static final String DEFAULT_BASENAME = "classpath:i18n/messages";
        public static final String DEFAULT_ENCODING = "UTF-8";
        public static final String DEFAULT_LOCALE_TAG = "zh-CN";

        private String basename = DEFAULT_BASENAME;
        private String defaultEncoding = DEFAULT_ENCODING;
        private boolean fallbackToSystemLocale = false;
        private boolean useCodeAsDefaultMessage = true;
        private String defaultLocaleTag = DEFAULT_LOCALE_TAG;
    }

    @Data
    public static class Mdc {
        public static final String DEFAULT_THREAD_NAME_PREFIX = "mdc-thread-";

        private String threadNamePrefix = DEFAULT_THREAD_NAME_PREFIX;
    }

    @Data
    public static class Cache {
        public static final String DEFAULT_MEMORY_CLEANUP_THREAD_PREFIX = "cache-cleanup";
        public static final String DEFAULT_DB_CLEANUP_THREAD_PREFIX = "cache-db-cleanup";

        private String memoryCleanupThreadPrefix = DEFAULT_MEMORY_CLEANUP_THREAD_PREFIX;
        private String dbCleanupThreadPrefix = DEFAULT_DB_CLEANUP_THREAD_PREFIX;
    }

    @Data
    public static class ExceptionHandling {
        public static final long DEFAULT_CLIENT_ABORT_WINDOW_MILLIS = 60_000L;
        public static final int DEFAULT_CLIENT_ABORT_WARN_THRESHOLD = 20;
        public static final int DEFAULT_CLIENT_ABORT_MESSAGE_MAX_LENGTH = 200;

        /**
         * 统计客户端断开连接的时间窗口（毫秒）。
         */
        private long clientAbortWindowMillis = DEFAULT_CLIENT_ABORT_WINDOW_MILLIS;
        /**
         * 断开连接达到该阈值时输出告警日志。
         */
        private int clientAbortWarnThreshold = DEFAULT_CLIENT_ABORT_WARN_THRESHOLD;
        /**
         * 客户端断开连接日志消息最大长度。
         */
        private int clientAbortMessageMaxLength = DEFAULT_CLIENT_ABORT_MESSAGE_MAX_LENGTH;
    }
}
