package com.example.demo.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.permission.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
