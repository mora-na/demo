package com.example.demo.permission.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.permission.dto.PermissionCreateRequest;
import com.example.demo.permission.dto.PermissionStatusRequest;
import com.example.demo.permission.dto.PermissionUpdateRequest;
import com.example.demo.permission.dto.PermissionVO;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionAdminController extends BaseController {

    private final PermissionService permissionService;

    @GetMapping
    @RequirePermission("permission:query")
    public CommonResult<List<PermissionVO>> list() {
        return success(toVOs(permissionService.list()));
    }

    @GetMapping("/{id}")
    @RequirePermission("permission:query")
    public CommonResult<PermissionVO> detail(@PathVariable Long id) {
        Permission permission = permissionService.getById(id);
        if (permission == null) {
            return error(404, "permission not found");
        }
        return success(toVO(permission));
    }

    @PostMapping
    @RequirePermission("permission:create")
    public CommonResult<PermissionVO> create(@Valid @RequestBody PermissionCreateRequest request) {
        if (existsCode(request.getCode(), null)) {
            return error(400, "permission code already exists");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setStatus(normalizeStatus(request.getStatus()));
        permissionService.save(permission);
        return success(toVO(permission));
    }

    @PutMapping("/{id}")
    @RequirePermission("permission:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        Permission existing = permissionService.getById(id);
        if (existing == null) {
            return error(404, "permission not found");
        }
        if (existsCode(request.getCode(), id)) {
            return error(400, "permission code already exists");
        }
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        if (!permissionService.updateById(permission)) {
            return error(500, "update failed");
        }
        return success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("permission:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PermissionStatusRequest request) {
        Permission existing = permissionService.getById(id);
        if (existing == null) {
            return error(404, "permission not found");
        }
        Integer status = request.getStatus();
        if (isNotValidStatus(status)) {
            return error(400, "invalid status");
        }
        if (!permissionService.updateStatus(id, status)) {
            return error(500, "update status failed");
        }
        return success();
    }

    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        Permission one = permissionService.getOne(Wrappers.lambdaQuery(Permission.class).eq(Permission::getCode, code).ne(excludeId != null, Permission::getId, excludeId));
        return one != null;
    }

    private Integer normalizeStatus(Integer status) {
        if (isNotValidStatus(status)) {
            return 1;
        }
        return status;
    }

    private boolean isNotValidStatus(Integer status) {
        return status == null || (status != 0 && status != 1);
    }

    private List<PermissionVO> toVOs(List<Permission> permissions) {
        return permissions.stream().map(this::toVO).collect(Collectors.toList());
    }

    private PermissionVO toVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setCode(permission.getCode());
        vo.setName(permission.getName());
        vo.setStatus(permission.getStatus());
        return vo;
    }
}
