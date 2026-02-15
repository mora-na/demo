package com.example.demo.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.mapper.DeptMapper;
import com.example.demo.dept.service.DeptService;
import org.springframework.stereotype.Service;

/**
 * 部门服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

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
}
