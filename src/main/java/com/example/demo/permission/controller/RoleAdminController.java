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

@Validated
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleAdminController extends BaseController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;

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

    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        Role one = roleService.getOne(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getCode, code)
                .ne(excludeId != null, Role::getId, excludeId));
        return one != null;
    }

    private Integer normalizeStatus(Integer status) {
        if (!isValidStatus(status)) {
            return 1;
        }
        return status;
    }

    private boolean isValidStatus(Integer status) {
        return status != null && (status == 0 || status == 1);
    }

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
