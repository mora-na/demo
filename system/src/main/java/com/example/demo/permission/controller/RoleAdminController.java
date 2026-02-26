package com.example.demo.permission.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.mybatis.DataScopeType;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.datascope.dto.RoleMenuDataScopeBatchRequest;
import com.example.demo.datascope.dto.RoleMenuDataScopeItemRequest;
import com.example.demo.datascope.dto.RoleMenuDataScopeItemVO;
import com.example.demo.datascope.dto.RoleMenuDataScopeResponse;
import com.example.demo.datascope.entity.RoleMenuDept;
import com.example.demo.datascope.service.RoleMenuDeptService;
import com.example.demo.menu.dto.RoleMenuAssignRequest;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.entity.RoleMenu;
import com.example.demo.menu.service.MenuService;
import com.example.demo.menu.service.RoleMenuService;
import com.example.demo.permission.config.PermissionConstants;
import com.example.demo.permission.dto.*;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.PermissionService;
import com.example.demo.permission.service.RolePermissionService;
import com.example.demo.permission.service.RoleService;
import com.example.demo.permission.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理后台接口，提供角色与权限关系的配置能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Validated
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleAdminController extends BaseController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;
    private final RoleMenuService roleMenuService;
    private final MenuService menuService;
    private final RoleMenuDeptService roleMenuDeptService;
    private final UserRoleService userRoleService;
    private final PermissionConstants permissionConstants;

    /**
     * 获取角色列表并附带权限集合。
     *
     * @return 角色列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping
    @RequirePermission("role:query")
    public CommonResult<List<RoleVO>> list() {
        List<Role> roles = roleService.list();
        List<Long> roleIds = roles.stream()
                .map(Role::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<RolePermission> relations = roleIds.isEmpty()
                ? Collections.emptyList()
                : rolePermissionService.list(Wrappers.lambdaQuery(RolePermission.class)
                .in(RolePermission::getRoleId, roleIds));
        Map<Long, List<Long>> permissionMap = relations.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId,
                        Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())));
        List<RoleVO> data = roles.stream()
                .map(role -> toVO(role, permissionMap.getOrDefault(role.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        return success(data);
    }

    /**
     * 查询角色详情。
     *
     * @param id 角色 ID
     * @return 角色详情
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping("/{id}")
    @RequirePermission("role:query")
    public CommonResult<RoleVO> detail(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        List<Long> permissionIds = rolePermissionService.list(Wrappers.lambdaQuery(RolePermission.class)
                        .eq(RolePermission::getRoleId, id))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        return success(toVO(role, permissionIds));
    }

    /**
     * 查询角色已分配菜单 ID 列表。
     *
     * @param id 角色 ID
     * @return 菜单 ID 列表
     */
    @GetMapping("/{id}/menus")
    @RequirePermission("role:query")
    public CommonResult<List<Long>> menuIds(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        List<Long> menuIds = roleMenuService.list(Wrappers.lambdaQuery(RoleMenu.class)
                        .eq(RoleMenu::getRoleId, id))
                .stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return success(menuIds);
    }

    /**
     * 创建角色。
     *
     * @param request 创建请求
     * @return 创建后的角色信息
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PostMapping
    @RequirePermission("role:create")
    public CommonResult<RoleVO> create(@Valid @RequestBody RoleCreateRequest request) {
        if (existsCode(request.getCode(), null)) {
            return error(permissionConstants.getController().getBadRequestCode(),
                    i18n(permissionConstants.getMessage().getRoleCodeExists()));
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setStatus(normalizeStatus(request.getStatus()));
        role.setDataScopeType(request.getDataScopeType());
        role.setDataScopeValue(request.getDataScopeValue());
        roleService.save(role);
        return success(toVO(role, Collections.emptyList()));
    }

    /**
     * 更新角色基础信息。
     *
     * @param id      角色 ID
     * @param request 更新请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}")
    @RequirePermission("role:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        Role existing = roleService.getById(id);
        if (existing == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        if (existsCode(request.getCode(), id)) {
            return error(permissionConstants.getController().getBadRequestCode(),
                    i18n(permissionConstants.getMessage().getRoleCodeExists()));
        }
        Role role = new Role();
        role.setId(id);
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDataScopeType(request.getDataScopeType());
        role.setDataScopeValue(request.getDataScopeValue());
        if (!roleService.updateById(role)) {
            return error(permissionConstants.getController().getInternalServerErrorCode(),
                    i18n(permissionConstants.getMessage().getCommonUpdateFailed()));
        }
        return success();
    }

    /**
     * 更新角色启用状态。
     *
     * @param id      角色 ID
     * @param request 状态请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}/status")
    @RequirePermission("role:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody RoleStatusRequest request) {
        Role existing = roleService.getById(id);
        if (existing == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        Integer status = request.getStatus();
        if (!isValidStatus(status)) {
            return error(permissionConstants.getController().getBadRequestCode(),
                    i18n(permissionConstants.getMessage().getCommonStatusInvalid()));
        }
        if (!roleService.updateStatus(id, status)) {
            return error(permissionConstants.getController().getInternalServerErrorCode(),
                    i18n(permissionConstants.getMessage().getCommonStatusUpdateFailed()));
        }
        return success();
    }

    /**
     * 为角色分配权限。
     *
     * @param id      角色 ID
     * @param request 权限分配请求
     * @return 分配结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}/permissions")
    @RequirePermission("role:permission:assign")
    public CommonResult<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionAssignRequest request) {
        Role existing = roleService.getById(id);
        if (existing == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        List<Long> permissionIds = request.getPermissionIds();
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> permissions = permissionService.listByIds(permissionIds);
            if (permissions.size() != permissionIds.stream().filter(Objects::nonNull).distinct().count()) {
                return error(permissionConstants.getController().getBadRequestCode(),
                        i18n(permissionConstants.getMessage().getPermissionNotFound()));
            }
        }
        if (!roleService.assignPermissions(id, permissionIds)) {
            return error(permissionConstants.getController().getInternalServerErrorCode(),
                    i18n(permissionConstants.getMessage().getRolePermissionsAssignFailed()));
        }
        return success();
    }

    /**
     * 为角色分配菜单权限。
     *
     * @param id      角色 ID
     * @param request 菜单分配请求
     * @return 分配结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}/menus")
    @RequirePermission("role:menu:assign")
    public CommonResult<Void> assignMenus(@PathVariable Long id, @Valid @RequestBody RoleMenuAssignRequest request) {
        Role existing = roleService.getById(id);
        if (existing == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        List<Long> menuIds = request.getMenuIds();
        if (menuIds != null && !menuIds.isEmpty()) {
            List<Menu> menus = menuService.listByIds(menuIds);
            if (menus.size() != menuIds.stream().filter(Objects::nonNull).distinct().count()) {
                return error(permissionConstants.getController().getBadRequestCode(),
                        i18n(permissionConstants.getMessage().getMenuNotFound()));
            }
        }
        if (!roleMenuService.assignMenus(id, menuIds)) {
            return error(permissionConstants.getController().getInternalServerErrorCode(),
                    i18n(permissionConstants.getMessage().getRoleMenusAssignFailed()));
        }
        return success();
    }

    /**
     * 获取角色菜单级数据范围配置。
     */
    @GetMapping("/{id}/menu-data-scope")
    @RequirePermission("role:menu:data-scope")
    public CommonResult<RoleMenuDataScopeResponse> getMenuDataScope(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        List<RoleMenu> roleMenus = roleMenuService.list(Wrappers.lambdaQuery(RoleMenu.class)
                .eq(RoleMenu::getRoleId, id));
        if (roleMenus == null || roleMenus.isEmpty()) {
            RoleMenuDataScopeResponse response = new RoleMenuDataScopeResponse();
            response.setRoleId(role.getId());
            response.setRoleCode(role.getCode());
            response.setRoleName(role.getName());
            response.setDefaultDataScopeType(role.getDataScopeType());
            response.setDefaultDataScopeValue(role.getDataScopeValue());
            response.setItems(Collections.emptyList());
            return success(response);
        }
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Menu> menuMap = menuService.listByIds(menuIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Menu::getId, menu -> menu, (a, b) -> a));

        Map<Long, List<Long>> customDeptMap = new HashMap<>();
        List<RoleMenuDept> customDeptRelations = roleMenuDeptService.list(
                Wrappers.lambdaQuery(RoleMenuDept.class).eq(RoleMenuDept::getRoleId, id));
        if (customDeptRelations != null) {
            for (RoleMenuDept relation : customDeptRelations) {
                if (relation == null || relation.getMenuId() == null || relation.getDeptId() == null) {
                    continue;
                }
                customDeptMap.computeIfAbsent(relation.getMenuId(), key -> new ArrayList<>()).add(relation.getDeptId());
            }
        }

        List<RoleMenuDataScopeItemVO> items = new ArrayList<>();
        for (RoleMenu roleMenu : roleMenus) {
            if (roleMenu == null) {
                continue;
            }
            Menu menu = menuMap.get(roleMenu.getMenuId());
            if (menu == null) {
                continue;
            }
            RoleMenuDataScopeItemVO item = new RoleMenuDataScopeItemVO();
            item.setMenuId(menu.getId());
            item.setMenuName(menu.getName());
            item.setParentId(menu.getParentId());
            item.setPermission(menu.getPermission());
            item.setDataScopeType(roleMenu.getDataScopeType());
            item.setCustomDeptIds(customDeptMap.getOrDefault(menu.getId(), Collections.emptyList()));
            items.add(item);
        }

        RoleMenuDataScopeResponse response = new RoleMenuDataScopeResponse();
        response.setRoleId(role.getId());
        response.setRoleCode(role.getCode());
        response.setRoleName(role.getName());
        response.setDefaultDataScopeType(role.getDataScopeType());
        response.setDefaultDataScopeValue(role.getDataScopeValue());
        response.setItems(items);
        return success(response);
    }

    /**
     * 批量保存角色菜单级数据范围配置。
     */
    @PutMapping("/{id}/menu-data-scope")
    @RequirePermission("role:menu:data-scope")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> saveMenuDataScope(@PathVariable Long id,
                                                @Valid @RequestBody RoleMenuDataScopeBatchRequest request) {
        Role role = roleService.getById(id);
        if (role == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return success();
        }
        for (RoleMenuDataScopeItemRequest item : request.getItems()) {
            if (item == null || item.getMenuId() == null) {
                continue;
            }
            RoleMenu relation = roleMenuService.getOne(Wrappers.lambdaQuery(RoleMenu.class)
                    .eq(RoleMenu::getRoleId, id)
                    .eq(RoleMenu::getMenuId, item.getMenuId()));
            if (relation == null) {
                return error(permissionConstants.getController().getBadRequestCode(),
                        i18n(permissionConstants.getMessage().getRoleMenuNotFound()));
            }
            String normalizedType = normalizeMenuScopeType(item.getDataScopeType());
            roleMenuService.update(Wrappers.lambdaUpdate(RoleMenu.class)
                    .eq(RoleMenu::getRoleId, id)
                    .eq(RoleMenu::getMenuId, item.getMenuId())
                    .set(RoleMenu::getDataScopeType, normalizedType));
            roleMenuDeptService.remove(Wrappers.lambdaQuery(RoleMenuDept.class)
                    .eq(RoleMenuDept::getRoleId, id)
                    .eq(RoleMenuDept::getMenuId, item.getMenuId()));
            if (isCustomScope(normalizedType)) {
                List<RoleMenuDept> relations = Optional.ofNullable(item.getCustomDeptIds())
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(deptId -> {
                            RoleMenuDept relationItem = new RoleMenuDept();
                            relationItem.setRoleId(id);
                            relationItem.setMenuId(item.getMenuId());
                            relationItem.setDeptId(deptId);
                            return relationItem;
                        })
                        .collect(Collectors.toList());
                if (!relations.isEmpty()) {
                    roleMenuDeptService.saveBatch(relations);
                }
            }
        }
        return success();
    }

    /**
     * 清除指定菜单的角色数据范围配置。
     */
    @DeleteMapping("/{roleId}/menu-data-scope/{menuId}")
    @RequirePermission("role:menu:data-scope")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> clearMenuDataScope(@PathVariable Long roleId, @PathVariable Long menuId) {
        Role role = roleService.getById(roleId);
        if (role == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        roleMenuService.update(Wrappers.lambdaUpdate(RoleMenu.class)
                .eq(RoleMenu::getRoleId, roleId)
                .eq(RoleMenu::getMenuId, menuId)
                .set(RoleMenu::getDataScopeType, null));
        roleMenuDeptService.remove(Wrappers.lambdaQuery(RoleMenuDept.class)
                .eq(RoleMenuDept::getRoleId, roleId)
                .eq(RoleMenuDept::getMenuId, menuId));
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("role:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (roleService.getById(id) == null) {
            return error(permissionConstants.getController().getNotFoundCode(),
                    i18n(permissionConstants.getMessage().getRoleNotFound()));
        }
        rolePermissionService.remove(Wrappers.lambdaQuery(RolePermission.class).eq(RolePermission::getRoleId, id));
        roleMenuService.remove(Wrappers.lambdaQuery(RoleMenu.class).eq(RoleMenu::getRoleId, id));
        userRoleService.remove(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getRoleId, id));
        if (!roleService.removeById(id)) {
            return error(permissionConstants.getController().getInternalServerErrorCode(),
                    i18n(permissionConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("role:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return success();
        }
        rolePermissionService.remove(Wrappers.lambdaQuery(RolePermission.class).in(RolePermission::getRoleId, uniqueIds));
        roleMenuService.remove(Wrappers.lambdaQuery(RoleMenu.class).in(RoleMenu::getRoleId, uniqueIds));
        userRoleService.remove(Wrappers.lambdaQuery(UserRole.class).in(UserRole::getRoleId, uniqueIds));
        if (!roleService.removeByIds(uniqueIds)) {
            return error(permissionConstants.getController().getInternalServerErrorCode(),
                    i18n(permissionConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    /**
     * 校验角色编码是否已存在。
     *
     * @param code      角色编码
     * @param excludeId 需排除的角色 ID
     * @return true 表示已存在
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        Role one = roleService.getOne(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getCode, code)
                .ne(excludeId != null, Role::getId, excludeId));
        return one != null;
    }

    /**
     * 规范化角色状态，非法值默认回退为启用。
     *
     * @param status 状态值
     * @return 规范化后的状态
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Integer normalizeStatus(Integer status) {
        if (!isValidStatus(status)) {
            return permissionConstants.getStatus().getEnabled();
        }
        return status;
    }

    /**
     * 判断角色状态是否合法。
     *
     * @param status 状态值
     * @return true 表示合法
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isValidStatus(Integer status) {
        return status != null
                && (status == permissionConstants.getStatus().getDisabled()
                || status == permissionConstants.getStatus().getEnabled());
    }

    private String normalizeMenuScopeType(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        String inherit = StringUtils.defaultString(permissionConstants.getMenuDataScope().getInherit())
                .trim()
                .toUpperCase(Locale.ROOT);
        if (normalized.equals(inherit)) {
            return null;
        }
        switch (normalized) {
            case DataScopeType.ALL:
            case DataScopeType.DEPT:
            case DataScopeType.DEPT_AND_CHILD:
            case DataScopeType.CUSTOM_DEPT:
            case DataScopeType.CUSTOM:
            case DataScopeType.SELF:
            case DataScopeType.NONE:
                return normalized;
            default:
                return null;
        }
    }

    private boolean isCustomScope(String type) {
        return DataScopeType.CUSTOM_DEPT.equals(type) || DataScopeType.CUSTOM.equals(type);
    }

    /**
     * 转换角色实体为 VO。
     *
     * @param role          角色实体
     * @param permissionIds 权限 ID 列表
     * @return 角色 VO
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private RoleVO toVO(Role role, List<Long> permissionIds) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setCode(role.getCode());
        vo.setName(role.getName());
        vo.setStatus(role.getStatus());
        vo.setDataScopeType(role.getDataScopeType());
        vo.setDataScopeValue(role.getDataScopeValue());
        vo.setPermissionIds(permissionIds);
        return vo;
    }
}
