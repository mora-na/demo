package com.example.demo.config.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Config 模块常量配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "config.constants")
public class ConfigConstants {

    private Cache cache = new Cache();
    private Status status = new Status();
    private Controller controller = new Controller();
    private Message message = new Message();
    private Group group = new Group();
    private Mask mask = new Mask();
    private HotUpdate hotUpdate = new HotUpdate();

    @Data
    public static class Cache {
        public static final String DEFAULT_KEY_PREFIX = "config:";
        public static final long DEFAULT_TTL_SECONDS = 300L;

        /**
         * 缓存 key 前缀。
         */
        private String keyPrefix = DEFAULT_KEY_PREFIX;
        /**
         * 缓存时长（秒），<=0 表示不缓存。
         */
        private long ttlSeconds = DEFAULT_TTL_SECONDS;
    }

    @Data
    public static class Status {
        public static final int DEFAULT_ENABLED = 1;
        public static final int DEFAULT_DISABLED = 0;

        /**
         * 启用状态值。
         */
        private int enabled = DEFAULT_ENABLED;
        /**
         * 禁用状态值。
         */
        private int disabled = DEFAULT_DISABLED;
    }

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        /**
         * 参数非法或业务冲突场景的错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 资源不存在场景的错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 更新/删除失败场景的错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_CONFIG_NOT_FOUND = "config.not.found";
        public static final String DEFAULT_CONFIG_KEY_EXISTS = "config.key.exists";
        public static final String DEFAULT_CONFIG_VALUE_INVALID = "config.value.invalid";
        public static final String DEFAULT_CONFIG_SCHEMA_INVALID = "config.schema.invalid";
        public static final String DEFAULT_COMMON_UPDATE_FAILED = "common.update.failed";
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";

        /**
         * 配置不存在消息键。
         */
        private String configNotFound = DEFAULT_CONFIG_NOT_FOUND;
        /**
         * 配置键冲突消息键。
         */
        private String configKeyExists = DEFAULT_CONFIG_KEY_EXISTS;
        /**
         * 配置值非法消息键。
         */
        private String configValueInvalid = DEFAULT_CONFIG_VALUE_INVALID;
        /**
         * 配置 Schema 非法消息键。
         */
        private String configSchemaInvalid = DEFAULT_CONFIG_SCHEMA_INVALID;
        /**
         * 通用更新失败消息键。
         */
        private String commonUpdateFailed = DEFAULT_COMMON_UPDATE_FAILED;
        /**
         * 通用删除失败消息键。
         */
        private String commonDeleteFailed = DEFAULT_COMMON_DELETE_FAILED;
    }

    @Data
    public static class Group {
        public static final String DEFAULT_GROUP = "default";

        /**
         * 默认分组。
         */
        private String defaultGroup = DEFAULT_GROUP;
    }

    @Data
    public static class Mask {
        public static final String DEFAULT_MASK_VALUE = "******";

        /**
         * 敏感配置展示的脱敏值。
         */
        private String maskValue = DEFAULT_MASK_VALUE;
    }

    @Data
    public static class HotUpdate {
        public static final int DEFAULT_ENABLED = 1;
        public static final int DEFAULT_DISABLED = 0;

        /**
         * 默认启用热更新标识。
         */
        private int enabled = DEFAULT_ENABLED;
        /**
         * 默认禁用热更新标识。
         */
        private int disabled = DEFAULT_DISABLED;
    }
}
