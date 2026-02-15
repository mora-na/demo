package com.example.demo.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * User 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "user.constants")
public class UserConstants {

    private Status status = new Status();
    private Controller controller = new Controller();
    private Message message = new Message();
    private Password password = new Password();
    private Page page = new Page();
    private Scope scope = new Scope();
    private Excel excel = new Excel();

    @Data
    public static class Status {
        public static final int DEFAULT_ENABLED = 1;
        public static final int DEFAULT_DISABLED = 0;
        public static final int DEFAULT_DATA_SCOPE_ENABLED = 1;

        /**
         * 用户启用状态值。
         */
        private int enabled = DEFAULT_ENABLED;
        /**
         * 用户禁用状态值。
         */
        private int disabled = DEFAULT_DISABLED;
        /**
         * 用户数据范围覆盖记录默认启用状态值。
         */
        private int dataScopeEnabled = DEFAULT_DATA_SCOPE_ENABLED;
    }

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_FORBIDDEN_CODE = 403;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        /**
         * 参数非法或业务冲突场景错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 无权限或越权场景错误码。
         */
        private int forbiddenCode = DEFAULT_FORBIDDEN_CODE;
        /**
         * 资源不存在场景错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 更新、删除、分配等服务执行失败场景错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_USER_NOT_FOUND = "user.not.found";
        public static final String DEFAULT_USER_USERNAME_EXISTS = "user.username.exists";
        public static final String DEFAULT_DEPT_NOT_FOUND = "dept.not.found";
        public static final String DEFAULT_USER_PASSWORD_EMPTY = "user.password.empty";
        public static final String DEFAULT_USER_PASSWORD_INVALID = "user.password.invalid";
        public static final String DEFAULT_USER_PASSWORD_LENGTH_INVALID = "user.password.length.invalid";
        public static final String DEFAULT_USER_PASSWORD_WEAK = "user.password.weak";
        public static final String DEFAULT_USER_PASSWORD_RESET_FAILED = "user.password.reset.failed";
        public static final String DEFAULT_USER_ROLES_ASSIGN_FAILED = "user.roles.assign.failed";
        public static final String DEFAULT_USER_POSTS_ASSIGN_FAILED = "user.posts.assign.failed";
        public static final String DEFAULT_USER_DATA_SCOPE_UPDATE_FAILED = "user.data.scope.update.failed";
        public static final String DEFAULT_DATA_SCOPE_USER_EXISTS = "data.scope.user.exists";
        public static final String DEFAULT_DATA_SCOPE_USER_NOT_FOUND = "data.scope.user.not.found";
        public static final String DEFAULT_COMMON_UPDATE_FAILED = "common.update.failed";
        public static final String DEFAULT_COMMON_STATUS_INVALID = "common.status.invalid";
        public static final String DEFAULT_COMMON_STATUS_UPDATE_FAILED = "common.status.update.failed";
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";
        public static final String DEFAULT_AUTH_PERMISSION_DENIED = "auth.permission.denied";
        public static final String DEFAULT_USER_IMPORT_SUCCESS = "user.import.success";
        public static final String DEFAULT_USER_IMPORT_FAILED = "user.import.failed";

        /**
         * 用户不存在消息键。
         */
        private String userNotFound = DEFAULT_USER_NOT_FOUND;
        /**
         * 用户名重复消息键。
         */
        private String userUsernameExists = DEFAULT_USER_USERNAME_EXISTS;
        /**
         * 部门不存在消息键。
         */
        private String deptNotFound = DEFAULT_DEPT_NOT_FOUND;
        /**
         * 密码为空消息键。
         */
        private String userPasswordEmpty = DEFAULT_USER_PASSWORD_EMPTY;
        /**
         * 密码解密或格式非法消息键。
         */
        private String userPasswordInvalid = DEFAULT_USER_PASSWORD_INVALID;
        /**
         * 密码长度非法消息键。
         */
        private String userPasswordLengthInvalid = DEFAULT_USER_PASSWORD_LENGTH_INVALID;
        /**
         * 密码强度不足消息键。
         */
        private String userPasswordWeak = DEFAULT_USER_PASSWORD_WEAK;
        /**
         * 重置密码失败消息键。
         */
        private String userPasswordResetFailed = DEFAULT_USER_PASSWORD_RESET_FAILED;
        /**
         * 分配角色失败消息键。
         */
        private String userRolesAssignFailed = DEFAULT_USER_ROLES_ASSIGN_FAILED;
        /**
         * 分配岗位失败消息键。
         */
        private String userPostsAssignFailed = DEFAULT_USER_POSTS_ASSIGN_FAILED;
        /**
         * 更新数据范围失败消息键。
         */
        private String userDataScopeUpdateFailed = DEFAULT_USER_DATA_SCOPE_UPDATE_FAILED;
        /**
         * 用户数据范围覆盖重复消息键。
         */
        private String dataScopeUserExists = DEFAULT_DATA_SCOPE_USER_EXISTS;
        /**
         * 用户数据范围覆盖不存在消息键。
         */
        private String dataScopeUserNotFound = DEFAULT_DATA_SCOPE_USER_NOT_FOUND;
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
        /**
         * 权限拒绝消息键。
         */
        private String authPermissionDenied = DEFAULT_AUTH_PERMISSION_DENIED;
        /**
         * 用户导入成功消息键。
         */
        private String userImportSuccess = DEFAULT_USER_IMPORT_SUCCESS;
        /**
         * 用户导入失败消息键。
         */
        private String userImportFailed = DEFAULT_USER_IMPORT_FAILED;
    }

    @Data
    public static class Password {
        public static final int DEFAULT_MIN_LENGTH = 6;

        /**
         * 用户创建/重置密码最小长度。
         */
        private int minLength = DEFAULT_MIN_LENGTH;
    }

    @Data
    public static class Page {
        public static final long DEFAULT_PAGE_NUM = 1L;
        public static final long DEFAULT_PAGE_SIZE = 10L;

        /**
         * 分页参数为空时默认页码。
         */
        private long defaultPageNum = DEFAULT_PAGE_NUM;
        /**
         * 分页参数为空时默认页大小。
         */
        private long defaultPageSize = DEFAULT_PAGE_SIZE;
    }

    @Data
    public static class Scope {
        public static final String DEFAULT_GLOBAL_SCOPE_KEY = "*";
        public static final String DEFAULT_GLOBAL_SCOPE_MENU_NAME = "全局覆盖";
        public static final String DEFAULT_GLOBAL_SCOPE_PERMISSION = "*";

        /**
         * 用户数据范围中的全局覆盖 scopeKey。
         */
        private String globalScopeKey = DEFAULT_GLOBAL_SCOPE_KEY;
        /**
         * 全局覆盖在列表展示中的菜单名称。
         */
        private String globalScopeMenuName = DEFAULT_GLOBAL_SCOPE_MENU_NAME;
        /**
         * 全局覆盖在列表展示中的权限标识。
         */
        private String globalScopePermission = DEFAULT_GLOBAL_SCOPE_PERMISSION;
    }

    @Data
    public static class Excel {
        public static final String DEFAULT_EXPORT_FILE_PREFIX = "用户信息";

        /**
         * 用户导出文件名默认前缀。
         */
        private String exportFilePrefix = DEFAULT_EXPORT_FILE_PREFIX;
    }
}
