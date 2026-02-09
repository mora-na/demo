package com.example.demo.permission.mapper;

import com.example.demo.common.mybatis.MppBaseMapper;
import com.example.demo.permission.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限数据访问层，提供权限相关查询。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface PermissionMapper extends MppBaseMapper<Permission> {

    /**
     * 根据用户 ID 查询其已启用角色的权限编码集合。
     *
     * @param userId 用户 ID
     * @return 权限编码列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Select("select distinct p.code from sys_permission p " +
            "join sys_role_permission rp on rp.permission_id = p.id " +
            "join sys_role r on r.id = rp.role_id " +
            "join sys_user_role ur on ur.role_id = rp.role_id " +
            "where ur.user_id = #{userId} and p.status = 1 and r.status = 1")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
