package com.example.demo.dict.config;

import com.example.demo.common.config.ConfigBinding;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dict 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@ConfigBinding(group = "dict", hotUpdate = true)
@Data
@Component
@ConfigurationProperties(prefix = "dict.constants")
public class DictConstants {

    private Status status = new Status();
    private Sort sort = new Sort();
    private Controller controller = new Controller();
    private Message message = new Message();
    private PublicApi publicApi = new PublicApi();
    private Cache cache = new Cache();
    private Serializer serializer = new Serializer();

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
    public static class Sort {
        public static final int DEFAULT_DEFAULT_SORT = 0;

        /**
         * 字典类型/数据默认排序值。
         */
        private int defaultSort = DEFAULT_DEFAULT_SORT;
    }

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        /**
         * 参数非法或业务冲突错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 资源不存在错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 更新/删除失败错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_DICT_TYPE_EXISTS = "dict.type.exists";
        public static final String DEFAULT_DICT_TYPE_NOT_FOUND = "dict.type.not.found";
        public static final String DEFAULT_DICT_DATA_EXISTS = "dict.data.exists";
        public static final String DEFAULT_DICT_DATA_NOT_FOUND = "dict.data.not.found";
        public static final String DEFAULT_COMMON_UPDATE_FAILED = "common.update.failed";
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";

        /**
         * 字典类型已存在消息键。
         */
        private String dictTypeExists = DEFAULT_DICT_TYPE_EXISTS;
        /**
         * 字典类型不存在消息键。
         */
        private String dictTypeNotFound = DEFAULT_DICT_TYPE_NOT_FOUND;
        /**
         * 字典数据已存在消息键。
         */
        private String dictDataExists = DEFAULT_DICT_DATA_EXISTS;
        /**
         * 字典数据不存在消息键。
         */
        private String dictDataNotFound = DEFAULT_DICT_DATA_NOT_FOUND;
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
    public static class PublicApi {
        public static final String DEFAULT_SUCCESS_MESSAGE = "success";
        public static final String DEFAULT_BATCH_TYPE_SEPARATOR = ",";

        /**
         * 公开字典接口成功返回文案。
         */
        private String successMessage = DEFAULT_SUCCESS_MESSAGE;
        /**
         * 批量查询字典类型时的分隔符。
         */
        private String batchTypeSeparator = DEFAULT_BATCH_TYPE_SEPARATOR;
    }

    @Data
    public static class Cache {
        public static final String DEFAULT_KEY_PREFIX = "dict:data:";
        public static final String DEFAULT_ALL_KEY = "dict:data:all";
        public static final long DEFAULT_TTL_SECONDS = 600;

        /**
         * 单类型缓存 key 前缀。
         */
        private String keyPrefix = DEFAULT_KEY_PREFIX;
        /**
         * 全量字典缓存 key。
         */
        private String allKey = DEFAULT_ALL_KEY;
        /**
         * 字典缓存 TTL（秒），<=0 表示不缓存。
         */
        private long ttlSeconds = DEFAULT_TTL_SECONDS;
    }

    @Data
    public static class Serializer {
        public static final String DEFAULT_LABEL_FIELD_SUFFIX = "Label";

        /**
         * 自动补充字典标签字段后缀。
         */
        private String labelFieldSuffix = DEFAULT_LABEL_FIELD_SUFFIX;
    }
}
