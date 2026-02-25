package com.example.demo.dept.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.annotation.DataScope;
import com.example.demo.dept.config.DeptConstants;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.mapper.DeptMapper;
import com.example.demo.dept.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final DeptConstants deptConstants;

    /**
     * 更新部门启用状态。
     *
     * @param deptId 部门 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean updateStatus(Long deptId, Integer status) {
        if (deptId == null) {
            return false;
        }
        Dept dept = new Dept();
        dept.setId(deptId);
        dept.setStatus(status);
        return updateById(dept);
    }

    @Override
    @DataScope(permission = "dept:query")
    public List<Dept> listByScope(boolean enabledOnly) {
        return list(Wrappers.lambdaQuery(Dept.class)
                .eq(enabledOnly, Dept::getStatus, deptConstants.getStatus().getEnabled())
                .orderByAsc(Dept::getSort, Dept::getId));
    }
}
