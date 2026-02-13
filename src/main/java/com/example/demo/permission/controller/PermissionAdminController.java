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
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.service.PermissionService;
import com.example.demo.permission.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理后台接口，提供权限的查询、创建、更新与状态控制。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Validated
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionAdminController extends BaseController {

    private final PermissionService permissionService;
    private final RolePermissionService rolePermissionService;

    /**
     * 获取权限列表。
     *
     * @return 权限列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping
    @RequirePermission("permission:query")
    public CommonResult<List<PermissionVO>> list() {
        return success(toVOs(permissionService.list()));
    }

    /**
     * 查询权限详情。
     *
     * @param id 权限 ID
     * @return 权限详情
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping("/{id}")
    @RequirePermission("permission:query")
    public CommonResult<PermissionVO> detail(@PathVariable Long id) {
        Permission permission = permissionService.getById(id);
        if (permission == null) {
            return error(404, i18n("permission.not.found"));
        }
        return success(toVO(permission));
    }

    /**
     * 创建权限。
     *
     * @param request 创建请求
     * @return 创建后的权限信息
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PostMapping
    @RequirePermission("permission:create")
    public CommonResult<PermissionVO> create(@Valid @RequestBody PermissionCreateRequest request) {
        if (existsCode(request.getCode(), null)) {
            return error(400, i18n("permission.code.exists"));
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setStatus(normalizeStatus(request.getStatus()));
        permissionService.save(permission);
        return success(toVO(permission));
    }

    /**
     * 更新权限基础信息。
     *
     * @param id      权限 ID
     * @param request 更新请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}")
    @RequirePermission("permission:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        Permission existing = permissionService.getById(id);
        if (existing == null) {
            return error(404, i18n("permission.not.found"));
        }
        if (existsCode(request.getCode(), id)) {
            return error(400, i18n("permission.code.exists"));
        }
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        if (!permissionService.updateById(permission)) {
            return error(500, i18n("common.update.failed"));
        }
        return success();
    }

    /**
     * 更新权限启用状态。
     *
     * @param id      权限 ID
     * @param request 状态请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}/status")
    @RequirePermission("permission:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PermissionStatusRequest request) {
        Permission existing = permissionService.getById(id);
        if (existing == null) {
            return error(404, i18n("permission.not.found"));
        }
        Integer status = request.getStatus();
        if (notValidStatus(status)) {
            return error(400, i18n("common.status.invalid"));
        }
        if (!permissionService.updateStatus(id, status)) {
            return error(500, i18n("common.status.update.failed"));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("permission:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (permissionService.getById(id) == null) {
            return error(404, i18n("permission.not.found"));
        }
        rolePermissionService.remove(Wrappers.lambdaQuery(RolePermission.class)
                .eq(RolePermission::getPermissionId, id));
        if (!permissionService.removeById(id)) {
            return error(500, i18n("common.delete.failed"));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("permission:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return success();
        }
        rolePermissionService.remove(Wrappers.lambdaQuery(RolePermission.class)
                .in(RolePermission::getPermissionId, uniqueIds));
        if (!permissionService.removeByIds(uniqueIds)) {
            return error(500, i18n("common.delete.failed"));
        }
        return success();
    }

    /**
     * 校验权限编码是否已存在。
     *
     * @param code      权限编码
     * @param excludeId 需排除的权限 ID
     * @return true 表示已存在
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        Permission one = permissionService.getOne(Wrappers.lambdaQuery(Permission.class).eq(Permission::getCode, code).ne(excludeId != null, Permission::getId, excludeId));
        return one != null;
    }

    /**
     * 规范化权限状态，非法值默认回退为启用。
     *
     * @param status 状态值
     * @return 规范化后的状态
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Integer normalizeStatus(Integer status) {
        if (notValidStatus(status)) {
            return 1;
        }
        return status;
    }

    /**
     * 判断状态值是否合法。
     *
     * @param status 状态值
     * @return true 表示非法
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean notValidStatus(Integer status) {
        return status == null || (status != 0 && status != 1);
    }

    /**
     * 批量转换权限实体为 VO。
     *
     * @param permissions 权限实体列表
     * @return 权限 VO 列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private List<PermissionVO> toVOs(List<Permission> permissions) {
        return permissions.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 转换权限实体为 VO。
     *
     * @param permission 权限实体
     * @return 权限 VO
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private PermissionVO toVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setCode(permission.getCode());
        vo.setName(permission.getName());
        vo.setStatus(permission.getStatus());
        return vo;
    }
}
