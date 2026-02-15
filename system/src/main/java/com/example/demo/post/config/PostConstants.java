package com.example.demo.post.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Post 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "post.constants")
public class PostConstants {

    private Status status = new Status();
    private Sort sort = new Sort();
    private Controller controller = new Controller();
    private Message message = new Message();

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
         * 创建岗位时未传排序值时使用的默认排序。
         */
        private int defaultSort = DEFAULT_DEFAULT_SORT;
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
         * 更新/删除等服务执行失败场景的错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_POST_NOT_FOUND = "post.not.found";
        public static final String DEFAULT_POST_CODE_EXISTS = "post.code.exists";
        public static final String DEFAULT_DEPT_NOT_FOUND = "dept.not.found";
        public static final String DEFAULT_COMMON_UPDATE_FAILED = "common.update.failed";
        public static final String DEFAULT_COMMON_STATUS_INVALID = "common.status.invalid";
        public static final String DEFAULT_COMMON_STATUS_UPDATE_FAILED = "common.status.update.failed";
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";

        /**
         * 岗位不存在消息键。
         */
        private String postNotFound = DEFAULT_POST_NOT_FOUND;
        /**
         * 岗位编码冲突消息键。
         */
        private String postCodeExists = DEFAULT_POST_CODE_EXISTS;
        /**
         * 部门不存在消息键。
         */
        private String deptNotFound = DEFAULT_DEPT_NOT_FOUND;
        /**
         * 通用更新失败消息键。
         */
        private String commonUpdateFailed = DEFAULT_COMMON_UPDATE_FAILED;
        /**
         * 状态值非法消息键。
         */
        private String commonStatusInvalid = DEFAULT_COMMON_STATUS_INVALID;
        /**
         * 状态更新失败消息键。
         */
        private String commonStatusUpdateFailed = DEFAULT_COMMON_STATUS_UPDATE_FAILED;
        /**
         * 通用删除失败消息键。
         */
        private String commonDeleteFailed = DEFAULT_COMMON_DELETE_FAILED;
    }
}
