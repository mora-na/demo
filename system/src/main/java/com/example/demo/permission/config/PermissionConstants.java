package com.example.demo.permission.config;

import com.example.demo.common.config.ConfigBinding;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Permission 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@ConfigBinding(group = "permission", hotUpdate = true)
@Data
@Component
@ConfigurationProperties(prefix = "permission.constants")
public class PermissionConstants {

    private Status status = new Status();
    private Controller controller = new Controller();
    private Message message = new Message();
    private MenuDataScope menuDataScope = new MenuDataScope();

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
         * 参数非法或业务冲突场景错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 资源不存在场景错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 服务执行失败场景错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_PERMISSION_NOT_FOUND = "permission.not.found";
        public static final String DEFAULT_PERMISSION_CODE_EXISTS = "permission.code.exists";
        public static final String DEFAULT_ROLE_NOT_FOUND = "role.not.found";
        public static final String DEFAULT_ROLE_CODE_EXISTS = "role.code.exists";
        public static final String DEFAULT_MENU_NOT_FOUND = "menu.not.found";
        public static final String DEFAULT_ROLE_MENU_NOT_FOUND = "role.menu.not.found";
        public static final String DEFAULT_ROLE_PERMISSIONS_ASSIGN_FAILED = "role.permissions.assign.failed";
        public static final String DEFAULT_ROLE_MENUS_ASSIGN_FAILED = "role.menus.assign.failed";
        public static final String DEFAULT_COMMON_UPDATE_FAILED = "common.update.failed";
        public static final String DEFAULT_COMMON_STATUS_INVALID = "common.status.invalid";
        public static final String DEFAULT_COMMON_STATUS_UPDATE_FAILED = "common.status.update.failed";
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";

        /**
         * 权限不存在消息键。
         */
        private String permissionNotFound = DEFAULT_PERMISSION_NOT_FOUND;
        /**
         * 权限编码冲突消息键。
         */
        private String permissionCodeExists = DEFAULT_PERMISSION_CODE_EXISTS;
        /**
         * 角色不存在消息键。
         */
        private String roleNotFound = DEFAULT_ROLE_NOT_FOUND;
        /**
         * 角色编码冲突消息键。
         */
        private String roleCodeExists = DEFAULT_ROLE_CODE_EXISTS;
        /**
         * 菜单不存在消息键。
         */
        private String menuNotFound = DEFAULT_MENU_NOT_FOUND;
        /**
         * 角色菜单关系不存在消息键。
         */
        private String roleMenuNotFound = DEFAULT_ROLE_MENU_NOT_FOUND;
        /**
         * 角色分配权限失败消息键。
         */
        private String rolePermissionsAssignFailed = DEFAULT_ROLE_PERMISSIONS_ASSIGN_FAILED;
        /**
         * 角色分配菜单失败消息键。
         */
        private String roleMenusAssignFailed = DEFAULT_ROLE_MENUS_ASSIGN_FAILED;
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

    @Data
    public static class MenuDataScope {
        public static final String DEFAULT_INHERIT = "INHERIT";
        public static final String DEFAULT_DEFAULT_TYPE = "DEFAULT";

        /**
         * 角色菜单数据范围继承标识（表示清空菜单级覆盖，回退到角色默认范围）。
         */
        private String inherit = DEFAULT_INHERIT;
        /**
         * 角色菜单数据范围默认标识（同 inherit，保留用于兼容前端表达）。
         */
        private String defaultType = DEFAULT_DEFAULT_TYPE;
    }
}
