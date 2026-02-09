package com.example.demo.permission.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.permission.dto.*;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.service.PermissionService;
import com.example.demo.permission.service.RolePermissionService;
import com.example.demo.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        Map<Long, List<Long>> permissionMap = rolePermissionService.list().stream()
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
            return error(404, "role not found");
        }
        List<Long> permissionIds = rolePermissionService.list(Wrappers.lambdaQuery(RolePermission.class)
                        .eq(RolePermission::getRoleId, id))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        return success(toVO(role, permissionIds));
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
            return error(400, "role code already exists");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setStatus(normalizeStatus(request.getStatus()));
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
            return error(404, "role not found");
        }
        if (existsCode(request.getCode(), id)) {
            return error(400, "role code already exists");
        }
        Role role = new Role();
        role.setId(id);
        role.setCode(request.getCode());
        role.setName(request.getName());
        if (!roleService.updateById(role)) {
            return error(500, "update failed");
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
            return error(404, "role not found");
        }
        Integer status = request.getStatus();
        if (!isValidStatus(status)) {
            return error(400, "invalid status");
        }
        if (!roleService.updateStatus(id, status)) {
            return error(500, "update status failed");
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
            return error(404, "role not found");
        }
        List<Long> permissionIds = request.getPermissionIds();
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> permissions = permissionService.listByIds(permissionIds);
            if (permissions.size() != permissionIds.stream().filter(pid -> pid != null).distinct().count()) {
                return error(400, "permission not found");
            }
        }
        if (!roleService.assignPermissions(id, permissionIds)) {
            return error(500, "assign permissions failed");
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
            return 1;
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
        return status != null && (status == 0 || status == 1);
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
        vo.setPermissionIds(permissionIds);
        return vo;
    }
}
