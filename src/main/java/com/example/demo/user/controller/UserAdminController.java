package com.example.demo.user.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.*;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAdminController extends BaseController {

    private final UserService userService;
    private final UserConverter userConverter;

    @PostMapping
    @RequirePermission("user:create")
    public CommonResult<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        if (userService.getByUserName(request.getUserName()) != null) {
            return error(400, "username already exists");
        }
        User created = userService.createUser(request);
        return success(userConverter.toView(created, java.util.Collections.emptyList()));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public CommonResult<Void> update(@PathVariable("id") Long id,
                                     @Valid @RequestBody UserUpdateRequest request) {
        User existing = userService.getById(id);
        if (existing == null) {
            return error(404, "user not found");
        }
        if (StringUtils.isNotBlank(request.getUserName())) {
            User sameName = userService.getByUserName(request.getUserName());
            if (sameName != null && !sameName.getId().equals(id)) {
                return error(400, "username already exists");
            }
        }
        if (!userService.updateUser(id, request)) {
            return error(500, "update failed");
        }
        return success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("user:disable")
    public CommonResult<Void> updateStatus(@PathVariable("id") Long id,
                                           @Valid @RequestBody UserStatusRequest request) {
        if (userService.getById(id) == null) {
            return error(404, "user not found");
        }
        Integer status = request.getStatus();
        if (status == null || (status != User.STATUS_ENABLED && status != User.STATUS_DISABLED)) {
            return error(400, "invalid status");
        }
        if (!userService.updateStatus(id, status)) {
            return error(500, "update status failed");
        }
        return success();
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("user:password:reset")
    public CommonResult<Void> resetPassword(@PathVariable("id") Long id,
                                            @Valid @RequestBody UserResetPasswordRequest request) {
        if (userService.getById(id) == null) {
            return error(404, "user not found");
        }
        if (!userService.resetPassword(id, request.getNewPassword())) {
            return error(500, "reset password failed");
        }
        return success();
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("user:role:assign")
    public CommonResult<Void> assignRoles(@PathVariable("id") Long id,
                                          @Valid @RequestBody UserRoleAssignRequest request) {
        if (userService.getById(id) == null) {
            return error(404, "user not found");
        }
        if (!userService.assignRoles(id, request.getRoleIds())) {
            return error(500, "assign roles failed");
        }
        return success();
    }

    @PutMapping("/{id}/data-scope")
    @RequirePermission("user:data-scope:set")
    public CommonResult<Void> updateDataScope(@PathVariable("id") Long id,
                                              @Valid @RequestBody UserDataScopeRequest request) {
        if (userService.getById(id) == null) {
            return error(404, "user not found");
        }
        if (!userService.updateDataScope(id, request.getDataScopeType(), request.getDataScopeValue())) {
            return error(500, "update data scope failed");
        }
        return success();
    }
}
