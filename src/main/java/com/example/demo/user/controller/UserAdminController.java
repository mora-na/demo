package com.example.demo.user.controller;

import com.example.demo.auth.service.PasswordService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dept.service.DeptService;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.*;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户后台管理接口，覆盖创建、更新、状态、角色与数据范围的维护操作。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAdminController extends BaseController {

    private final UserService userService;
    private final UserConverter userConverter;
    private final DeptService deptService;
    private final PasswordService passwordService;

    @PostMapping
    @RequirePermission("user:create")
    public CommonResult<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        if (userService.getByUserName(request.getUserName()) != null) {
            return error(400, i18n("user.username.exists"));
        }
        if (request.getDeptId() != null && deptService.getById(request.getDeptId()) == null) {
            return error(400, i18n("dept.not.found"));
        }
        String rawPassword = passwordService.resolveRawPassword(request.getPassword());
        if (StringUtils.isBlank(rawPassword)) {
            return error(400, i18n("user.password.empty"));
        }
        if (rawPassword.length() < 6) {
            return error(400, i18n("user.password.length.invalid"));
        }
        if (!passwordService.isStrongPassword(rawPassword)) {
            return error(400, i18n("user.password.weak"));
        }
        request.setPassword(rawPassword);
        User created = userService.createUser(request);
        return success(userConverter.toView(created, java.util.Collections.emptyList()));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User existing = userService.getById(id);
        if (existing == null) {
            return error(404, i18n("user.not.found"));
        }
        if (StringUtils.isNotBlank(request.getUserName())) {
            User sameName = userService.getByUserName(request.getUserName());
            if (sameName != null && !sameName.getId().equals(id)) {
                return error(400, i18n("user.username.exists"));
            }
        }
        if (request.getDeptId() != null && deptService.getById(request.getDeptId()) == null) {
            return error(400, i18n("dept.not.found"));
        }
        if (!userService.updateUser(id, request)) {
            return error(500, i18n("common.update.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("user:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        Integer status = request.getStatus();
        if (status == null || (status != User.STATUS_ENABLED && status != User.STATUS_DISABLED)) {
            return error(400, i18n("common.status.invalid"));
        }
        if (!userService.updateStatus(id, status)) {
            return error(500, i18n("common.status.update.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("user:password:reset")
    public CommonResult<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserResetPasswordRequest request) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        String rawPassword = passwordService.decodeTransportPassword(request.getNewPassword());
        if (StringUtils.isBlank(rawPassword)) {
            return error(400, i18n("user.password.invalid"));
        }
        if (rawPassword.length() < 6) {
            return error(400, i18n("user.password.length.invalid"));
        }
        if (!passwordService.isStrongPassword(rawPassword)) {
            return error(400, i18n("user.password.weak"));
        }
        if (!userService.resetPassword(id, rawPassword)) {
            return error(500, i18n("user.password.reset.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("user:role:assign")
    public CommonResult<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleAssignRequest request) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        if (!userService.assignRoles(id, request.getRoleIds())) {
            return error(500, i18n("user.roles.assign.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/data-scope")
    @RequirePermission("user:data-scope:set")
    public CommonResult<Void> updateDataScope(@PathVariable Long id, @Valid @RequestBody UserDataScopeRequest request) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        if (!userService.updateDataScope(id, request.getDataScopeType(), request.getDataScopeValue())) {
            return error(500, i18n("user.data.scope.update.failed"));
        }
        return success();
    }
}
