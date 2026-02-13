package com.example.demo.user.controller;

import com.example.demo.auth.service.PasswordService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dept.service.DeptService;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.user.converter.SysUserConverter;
import com.example.demo.user.dto.*;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import com.example.demo.user.service.SysUserViewService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
public class SysUserAdminController extends BaseController {

    private final SysUserService userService;
    private final SysUserConverter userConverter;
    private final SysUserViewService userViewService;
    private final UserRoleService userRoleService;
    private final DeptService deptService;
    private final PasswordService passwordService;

    /**
     * 获取用户列表。
     *
     * @param query 查询参数
     * @return 用户分页列表
     */
    @GetMapping
    @RequirePermission("user:query")
    public CommonResult<PageResult<SysUserVO>> list(@ModelAttribute SysUserQuery query) {
        return success(page(query, userService::selectUsers, userViewService::toView));
    }

    /**
     * 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @RequirePermission("user:query")
    public CommonResult<SysUserVO> detail(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return error(404, i18n("user.not.found"));
        }
        return success(userViewService.toView(user));
    }

    /**
     * 查询用户已分配角色 ID 列表。
     *
     * @param id 用户 ID
     * @return 角色 ID 列表
     */
    @GetMapping("/{id}/roles")
    @RequirePermission("user:query")
    public CommonResult<java.util.List<Long>> userRoleIds(@PathVariable Long id) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        java.util.List<Long> roleIds = userRoleService.list(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserRole.class)
                                .eq(UserRole::getUserId, id))
                .stream()
                .map(UserRole::getRoleId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        return success(roleIds);
    }

    @PostMapping
    @RequirePermission("user:create")
    public CommonResult<SysUserVO> create(@Valid @RequestBody SysUserCreateRequest request) {
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
        SysUser created = userService.createUser(request);
        return success(userConverter.toView(created, java.util.Collections.emptyList()));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserUpdateRequest request) {
        SysUser existing = userService.getById(id);
        if (existing == null) {
            return error(404, i18n("user.not.found"));
        }
        if (StringUtils.isNotBlank(request.getUserName())) {
            SysUser sameName = userService.getByUserName(request.getUserName());
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
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody SysUserStatusRequest request) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        Integer status = request.getStatus();
        if (status == null || (status != SysUser.STATUS_ENABLED && status != SysUser.STATUS_DISABLED)) {
            return error(400, i18n("common.status.invalid"));
        }
        if (!userService.updateStatus(id, status)) {
            return error(500, i18n("common.status.update.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("user:password:reset")
    public CommonResult<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody SysUserResetPasswordRequest request) {
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
    public CommonResult<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody SysUserRoleAssignRequest request) {
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
    public CommonResult<Void> updateDataScope(@PathVariable Long id, @Valid @RequestBody SysUserDataScopeRequest request) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        if (!userService.updateDataScope(id, request.getDataScopeType(), request.getDataScopeValue())) {
            return error(500, i18n("user.data.scope.update.failed"));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (userService.getById(id) == null) {
            return error(404, i18n("user.not.found"));
        }
        userRoleService.remove(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, id));
        if (!userService.removeById(id)) {
            return error(500, i18n("common.delete.failed"));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("user:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return success();
        }
        userRoleService.remove(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserRole.class)
                .in(UserRole::getUserId, uniqueIds));
        if (!userService.removeByIds(uniqueIds)) {
            return error(500, i18n("common.delete.failed"));
        }
        return success();
    }
}
