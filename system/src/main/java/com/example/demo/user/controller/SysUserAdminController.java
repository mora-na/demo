package com.example.demo.user.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.datascope.dto.UserDataScopeCreateRequest;
import com.example.demo.datascope.dto.UserDataScopeDetailResponse;
import com.example.demo.datascope.dto.UserDataScopeUpdateRequest;
import com.example.demo.datascope.dto.UserDataScopeVO;
import com.example.demo.datascope.entity.UserDataScope;
import com.example.demo.datascope.service.UserDataScopeService;
import com.example.demo.dept.service.DeptService;
import com.example.demo.identity.api.service.UserPasswordPolicyService;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.service.MenuService;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.post.entity.UserPost;
import com.example.demo.post.service.UserPostService;
import com.example.demo.user.config.UserConstants;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final UserPostService userPostService;
    private final DeptService deptService;
    private final UserPasswordPolicyService userPasswordPolicyService;
    private final UserDataScopeService userDataScopeService;
    private final MenuService menuService;
    private final UserConstants userConstants;

    /**
     * 获取用户列表。
     *
     * @param query 查询参数
     * @return 用户分页列表
     */
    @GetMapping
    @RequirePermission("user:query")
    public CommonResult<PageResult<SysUserVO>> list(@ModelAttribute SysUserQuery query) {
        return success(page(query, userService::selectUsersPage, userViewService::toView));
    }

    /**
     * 用户关键字搜索（用户名/昵称）。
     *
     * @param keyword 关键字
     * @return 用户分页列表
     */
    @GetMapping("/search")
    @RequirePermission("user:query")
    public CommonResult<PageResult<SysUserVO>> search(@RequestParam(value = "keyword", required = false) String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return success(emptyPage());
        }
        return success(page(page -> userService.searchUsersPage(page, keyword), userViewService::toView));
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
        SysUser user = userService.getByIdScoped(id);
        if (user == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
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
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
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

    /**
     * 查询用户已分配岗位 ID 列表。
     *
     * @param id 用户 ID
     * @return 岗位 ID 列表
     */
    @GetMapping("/{id}/posts")
    @RequirePermission("user:query")
    public CommonResult<java.util.List<Long>> userPostIds(@PathVariable Long id) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        java.util.List<Long> postIds = userPostService.list(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserPost.class)
                                .eq(UserPost::getUserId, id))
                .stream()
                .map(UserPost::getPostId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        return success(postIds);
    }

    @PostMapping
    @RequirePermission("user:create")
    public CommonResult<SysUserVO> create(@Valid @RequestBody SysUserCreateRequest request) {
        if (userService.getByUserName(request.getUserName()) != null) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserUsernameExists()));
        }
        if (request.getDeptId() != null && deptService.getById(request.getDeptId()) == null) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getDeptNotFound()));
        }
        String rawPassword = userPasswordPolicyService.resolveRawPassword(request.getPassword());
        if (StringUtils.isBlank(rawPassword)) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserPasswordEmpty()));
        }
        if (rawPassword.length() < userConstants.getPassword().getMinLength()) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserPasswordLengthInvalid()));
        }
        if (!userPasswordPolicyService.isStrongPassword(rawPassword)) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserPasswordWeak()));
        }
        request.setPassword(rawPassword);
        SysUser created = userService.createUser(request);
        return success(userConverter.toView(created));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserUpdateRequest request) {
        SysUser existing = userService.getByIdScoped(id);
        if (existing == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        if (StringUtils.isNotBlank(request.getUserName())) {
            SysUser sameName = userService.getByUserName(request.getUserName());
            if (sameName != null && !sameName.getId().equals(id)) {
                return error(userConstants.getController().getBadRequestCode(),
                        i18n(userConstants.getMessage().getUserUsernameExists()));
            }
        }
        if (request.getDeptId() != null && deptService.getById(request.getDeptId()) == null) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getDeptNotFound()));
        }
        if (!userService.updateUser(id, request)) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getCommonUpdateFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("user:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody SysUserStatusRequest request) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        Integer status = request.getStatus();
        if (status == null || (status != userConstants.getStatus().getEnabled()
                && status != userConstants.getStatus().getDisabled())) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getCommonStatusInvalid()));
        }
        if (!userService.updateStatus(id, status)) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getCommonStatusUpdateFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("user:password:reset")
    public CommonResult<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody SysUserResetPasswordRequest request) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        String rawPassword = userPasswordPolicyService.decodeTransportPassword(request.getNewPassword());
        if (StringUtils.isBlank(rawPassword)) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserPasswordInvalid()));
        }
        if (rawPassword.length() < userConstants.getPassword().getMinLength()) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserPasswordLengthInvalid()));
        }
        if (!userPasswordPolicyService.isStrongPassword(rawPassword)) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getUserPasswordWeak()));
        }
        if (!userService.resetPassword(id, rawPassword)) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getUserPasswordResetFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("user:role:assign")
    public CommonResult<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody SysUserRoleAssignRequest request) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        if (!userService.assignRoles(id, request.getRoleIds())) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getUserRolesAssignFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/posts")
    @RequirePermission("user:post:assign")
    public CommonResult<Void> assignPosts(@PathVariable Long id, @Valid @RequestBody SysUserPostAssignRequest request) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        if (!userService.assignPosts(id, request.getPostIds())) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getUserPostsAssignFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/data-scope")
    @RequirePermission("user:data-scope:set")
    public CommonResult<Void> updateDataScope(@PathVariable Long id, @Valid @RequestBody SysUserDataScopeRequest request) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        if (!userService.updateDataScope(id, request.getDataScopeType(), request.getDataScopeValue(), request.getScopeKey())) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getUserDataScopeUpdateFailed()));
        }
        return success();
    }

    @GetMapping("/{id}/data-scope")
    @RequirePermission("data-scope:user:query")
    public CommonResult<UserDataScopeDetailResponse> listUserDataScopes(@PathVariable Long id) {
        SysUser user = userService.getByIdScoped(id);
        if (user == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        UserDataScopeDetailResponse response = new UserDataScopeDetailResponse();
        response.setUserId(user.getId());
        response.setUserName(user.getUserName());
        response.setNickName(user.getNickName());
        response.setDeptId(user.getDeptId());
        if (user.getDeptId() != null) {
            com.example.demo.dept.entity.Dept dept = deptService.getById(user.getDeptId());
            response.setDeptName(dept == null ? null : dept.getName());
        }
        List<UserDataScope> overrides = userDataScopeService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserDataScope.class)
                        .eq(UserDataScope::getUserId, id));
        response.setOverrides(toUserDataScopeVOs(overrides));
        return success(response);
    }

    @PostMapping("/{id}/data-scope")
    @RequirePermission("data-scope:user:manage")
    public CommonResult<UserDataScopeVO> createUserDataScope(@PathVariable Long id,
                                                             @Valid @RequestBody UserDataScopeCreateRequest request) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        String scopeKey = normalizeScopeKey(request.getScopeKey());
        UserDataScope existing = userDataScopeService.getOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserDataScope.class)
                        .eq(UserDataScope::getUserId, id)
                        .eq(UserDataScope::getScopeKey, scopeKey));
        if (existing != null) {
            return error(userConstants.getController().getBadRequestCode(),
                    i18n(userConstants.getMessage().getDataScopeUserExists()));
        }
        UserDataScope record = new UserDataScope();
        record.setUserId(id);
        record.setScopeKey(scopeKey);
        record.setDataScopeType(request.getDataScopeType());
        record.setDataScopeValue(request.getDataScopeValue());
        record.setStatus(request.getStatus() == null
                ? userConstants.getStatus().getDataScopeEnabled()
                : request.getStatus());
        record.setRemark(request.getRemark());
        userDataScopeService.save(record);
        return success(toUserDataScopeVO(record));
    }

    @PutMapping("/data-scope/{scopeId}")
    @RequirePermission("data-scope:user:manage")
    public CommonResult<Void> updateUserDataScope(@PathVariable Long scopeId,
                                                  @Valid @RequestBody UserDataScopeUpdateRequest request) {
        UserDataScope existing = userDataScopeService.getById(scopeId);
        if (existing == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getDataScopeUserNotFound()));
        }
        UserDataScope record = new UserDataScope();
        record.setId(scopeId);
        record.setDataScopeType(request.getDataScopeType());
        record.setDataScopeValue(request.getDataScopeValue());
        record.setStatus(request.getStatus());
        record.setRemark(request.getRemark());
        if (!userDataScopeService.updateById(record)) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getCommonUpdateFailed()));
        }
        return success();
    }

    @DeleteMapping("/data-scope/{scopeId}")
    @RequirePermission("data-scope:user:manage")
    public CommonResult<Void> deleteUserDataScope(@PathVariable Long scopeId) {
        if (userDataScopeService.getById(scopeId) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getDataScopeUserNotFound()));
        }
        if (!userDataScopeService.removeById(scopeId)) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    private String normalizeScopeKey(String scopeKey) {
        if (StringUtils.isBlank(scopeKey)) {
            return userConstants.getScope().getGlobalScopeKey();
        }
        String trimmed = scopeKey.trim();
        return trimmed.isEmpty() ? userConstants.getScope().getGlobalScopeKey() : trimmed;
    }

    private List<UserDataScopeVO> toUserDataScopeVOs(List<UserDataScope> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return Collections.emptyList();
        }
        return overrides.stream()
                .filter(Objects::nonNull)
                .map(this::toUserDataScopeVO)
                .collect(Collectors.toList());
    }

    private UserDataScopeVO toUserDataScopeVO(UserDataScope entity) {
        UserDataScopeVO vo = new UserDataScopeVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setScopeKey(entity.getScopeKey());
        vo.setDataScopeType(entity.getDataScopeType());
        vo.setDataScopeValue(entity.getDataScopeValue());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        SysUser user = entity.getUserId() == null ? null : userService.getByIdScoped(entity.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setNickName(user.getNickName());
            vo.setDeptId(user.getDeptId());
            vo.setDeptName(user.getDeptId() == null ? null : deptService.getById(user.getDeptId()).getName());
        }
        if (StringUtils.isNotBlank(entity.getScopeKey())
                && !userConstants.getScope().getGlobalScopeKey().equals(entity.getScopeKey())) {
            Menu menu = menuService.getOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(Menu.class)
                    .eq(Menu::getPermission, entity.getScopeKey()));
            if (menu != null) {
                vo.setMenuName(menu.getName());
                vo.setPermission(menu.getPermission());
            }
        }
        if (userConstants.getScope().getGlobalScopeKey().equals(entity.getScopeKey())) {
            vo.setMenuName(userConstants.getScope().getGlobalScopeMenuName());
            vo.setPermission(userConstants.getScope().getGlobalScopePermission());
        }
        return vo;
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (userService.getByIdScoped(id) == null) {
            return error(userConstants.getController().getNotFoundCode(),
                    i18n(userConstants.getMessage().getUserNotFound()));
        }
        if (!userService.deleteUserScoped(id)) {
            return error(userConstants.getController().getInternalServerErrorCode(),
                    i18n(userConstants.getMessage().getCommonDeleteFailed()));
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
        if (!userService.deleteUsersScoped(uniqueIds)) {
            return error(userConstants.getController().getForbiddenCode(),
                    i18n(userConstants.getMessage().getAuthPermissionDenied()));
        }
        return success();
    }
}
