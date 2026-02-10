package com.example.demo.dept.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dept.dto.DeptCreateRequest;
import com.example.demo.dept.dto.DeptStatusRequest;
import com.example.demo.dept.dto.DeptUpdateRequest;
import com.example.demo.dept.dto.DeptVO;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
            return error(404, "dept not found");
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
            return error(400, "dept code already exists");
        }
        if (request.getParentId() != null && deptService.getById(request.getParentId()) == null) {
            return error(400, "parent dept not found");
        }
        Dept dept = new Dept();
        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setParentId(request.getParentId());
        dept.setStatus(normalizeStatus(request.getStatus()));
        dept.setSort(request.getSort() == null ? 0 : request.getSort());
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
            return error(404, "dept not found");
        }
        if (existsCode(request.getCode(), id)) {
            return error(400, "dept code already exists");
        }
        if (request.getParentId() != null) {
            if (id.equals(request.getParentId())) {
                return error(400, "parent dept cannot be self");
            }
            if (deptService.getById(request.getParentId()) == null) {
                return error(400, "parent dept not found");
            }
        }
        Dept dept = new Dept();
        dept.setId(id);
        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setParentId(request.getParentId());
        dept.setStatus(request.getStatus());
        dept.setSort(request.getSort());
        dept.setRemark(request.getRemark());
        if (!deptService.updateById(dept)) {
            return error(500, "update failed");
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
            return error(404, "dept not found");
        }
        Integer status = request.getStatus();
        if (notValidStatus(status)) {
            return error(400, "invalid status");
        }
        if (!deptService.updateStatus(id, status)) {
            return error(500, "update status failed");
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
