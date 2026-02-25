package com.example.demo.dept.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dept.entity.Dept;

/**
 * 部门服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface DeptService extends IService<Dept> {

    /**
     * 更新部门状态。
     *
     * @param deptId 部门 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean updateStatus(Long deptId, Integer status);

    /**
     * 获取部门列表（应用数据范围过滤）。
     *
     * @param enabledOnly 是否仅返回启用部门
     * @return 部门列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/25
     */
    java.util.List<Dept> listByScope(boolean enabledOnly);
}
