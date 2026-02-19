package com.example.demo.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.permission.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
