package com.example.demo.extension.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 动态接口常量配置。
 */
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

        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
        private int serviceUnavailableCode = DEFAULT_SERVICE_UNAVAILABLE_CODE;
        private int rateLimitCode = DEFAULT_RATE_LIMIT_CODE;
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
    }

    @Data
    public static class Http {
        public static final String DEFAULT_EXT_PREFIX = "/ext/";
        public static final String DEFAULT_ERROR_PATH = "/error";
        public static final String DEFAULT_ACTUATOR_PREFIX = "/actuator";

        private String extPrefix = DEFAULT_EXT_PREFIX;
        private String errorPath = DEFAULT_ERROR_PATH;
        private String actuatorPrefix = DEFAULT_ACTUATOR_PREFIX;
        private List<String> supportedMethods = new ArrayList<>(
                Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE")
        );
    }

    @Data
    public static class Execute {
        public static final int DEFAULT_LOG_MAX_LENGTH = 2000;

        private int logMaxLength = DEFAULT_LOG_MAX_LENGTH;
    }
}
