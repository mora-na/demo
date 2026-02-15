package com.example.demo.dept.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dept.config.DeptConstants;
import com.example.demo.dept.dto.DeptCreateRequest;
import com.example.demo.dept.dto.DeptStatusRequest;
import com.example.demo.dept.dto.DeptUpdateRequest;
import com.example.demo.dept.dto.DeptVO;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理后台接口，提供部门的查询、创建、更新与状态控制。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Validated
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptAdminController extends BaseController {

    private final DeptService deptService;
    private final DeptConstants deptConstants;

    /**
     * 获取部门列表。
     *
     * @return 部门列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping
    @RequirePermission("dept:query")
    public CommonResult<List<DeptVO>> list() {
        return success(toVOs(deptService.list()));
    }

    /**
     * 查询部门详情。
     *
     * @param id 部门 ID
     * @return 部门详情
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping("/{id}")
    @RequirePermission("dept:query")
    public CommonResult<DeptVO> detail(@PathVariable Long id) {
        Dept dept = deptService.getById(id);
        if (dept == null) {
            return error(deptConstants.getController().getNotFoundCode(), i18n(deptConstants.getMessage().getDeptNotFound()));
        }
        return success(toVO(dept));
    }

    /**
     * 创建部门。
     *
     * @param request 创建请求
     * @return 创建后的部门信息
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PostMapping
    @RequirePermission("dept:create")
    public CommonResult<DeptVO> create(@Valid @RequestBody DeptCreateRequest request) {
        if (existsCode(request.getCode(), null)) {
            return error(deptConstants.getController().getBadRequestCode(), i18n(deptConstants.getMessage().getDeptCodeExists()));
        }
        if (request.getParentId() != null && deptService.getById(request.getParentId()) == null) {
            return error(deptConstants.getController().getBadRequestCode(), i18n(deptConstants.getMessage().getDeptParentNotFound()));
        }
        Dept dept = new Dept();
        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setParentId(request.getParentId());
        dept.setStatus(normalizeStatus(request.getStatus()));
        dept.setSort(request.getSort() == null ? deptConstants.getSort().getDefaultSort() : request.getSort());
        dept.setRemark(request.getRemark());
        deptService.save(dept);
        return success(toVO(dept));
    }

    /**
     * 更新部门基础信息。
     *
     * @param id      部门 ID
     * @param request 更新请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}")
    @RequirePermission("dept:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody DeptUpdateRequest request) {
        Dept existing = deptService.getById(id);
        if (existing == null) {
            return error(deptConstants.getController().getNotFoundCode(), i18n(deptConstants.getMessage().getDeptNotFound()));
        }
        if (existsCode(request.getCode(), id)) {
            return error(deptConstants.getController().getBadRequestCode(), i18n(deptConstants.getMessage().getDeptCodeExists()));
        }
        if (request.getParentId() != null) {
            if (id.equals(request.getParentId())) {
                return error(deptConstants.getController().getBadRequestCode(), i18n(deptConstants.getMessage().getDeptParentCannotSelf()));
            }
            if (deptService.getById(request.getParentId()) == null) {
                return error(deptConstants.getController().getBadRequestCode(), i18n(deptConstants.getMessage().getDeptParentNotFound()));
            }
        }
        Dept dept = new Dept();
        dept.setId(id);
        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setParentId(request.getParentId());
        dept.setStatus(normalizeStatus(request.getStatus()));
        dept.setSort(request.getSort());
        dept.setRemark(request.getRemark());
        if (!deptService.updateById(dept)) {
            return error(deptConstants.getController().getInternalServerErrorCode(), i18n(deptConstants.getMessage().getCommonUpdateFailed()));
        }
        return success();
    }

    /**
     * 更新部门启用状态。
     *
     * @param id      部门 ID
     * @param request 状态请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}/status")
    @RequirePermission("dept:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody DeptStatusRequest request) {
        Dept existing = deptService.getById(id);
        if (existing == null) {
            return error(deptConstants.getController().getNotFoundCode(), i18n(deptConstants.getMessage().getDeptNotFound()));
        }
        Integer status = request.getStatus();
        if (notValidStatus(status)) {
            return error(deptConstants.getController().getBadRequestCode(), i18n(deptConstants.getMessage().getCommonStatusInvalid()));
        }
        if (!deptService.updateStatus(id, status)) {
            return error(deptConstants.getController().getInternalServerErrorCode(), i18n(deptConstants.getMessage().getCommonStatusUpdateFailed()));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("dept:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (deptService.getById(id) == null) {
            return error(deptConstants.getController().getNotFoundCode(), i18n(deptConstants.getMessage().getDeptNotFound()));
        }
        if (!deptService.removeById(id)) {
            return error(deptConstants.getController().getInternalServerErrorCode(), i18n(deptConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("dept:delete")
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
        if (!deptService.removeByIds(uniqueIds)) {
            return error(deptConstants.getController().getInternalServerErrorCode(), i18n(deptConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    /**
     * 校验部门编码是否已存在。
     *
     * @param code      部门编码
     * @param excludeId 需排除的部门 ID
     * @return true 表示已存在
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        Dept one = deptService.getOne(Wrappers.lambdaQuery(Dept.class).eq(Dept::getCode, code)
                .ne(excludeId != null, Dept::getId, excludeId));
        return one != null;
    }

    /**
     * 规范化部门状态，非法值默认回退为启用。
     *
     * @param status 状态值
     * @return 规范化后的状态
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Integer normalizeStatus(Integer status) {
        if (notValidStatus(status)) {
            return deptConstants.getStatus().getEnabled();
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
        return status == null
                || (status != deptConstants.getStatus().getDisabled()
                && status != deptConstants.getStatus().getEnabled());
    }

    /**
     * 转换部门实体为 VO。
     *
     * @param dept 部门实体
     * @return 部门 VO
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private DeptVO toVO(Dept dept) {
        if (dept == null) {
            return new DeptVO();
        }
        DeptVO view = new DeptVO();
        view.setId(dept.getId());
        view.setName(dept.getName());
        view.setCode(dept.getCode());
        view.setParentId(dept.getParentId());
        view.setStatus(dept.getStatus());
        view.setSort(dept.getSort());
        view.setRemark(dept.getRemark());
        return view;
    }

    /**
     * 批量转换部门实体为 VO。
     *
     * @param depts 部门实体列表
     * @return 部门 VO 列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private List<DeptVO> toVOs(List<Dept> depts) {
        return depts.stream().map(this::toVO).collect(Collectors.toList());
    }
}
