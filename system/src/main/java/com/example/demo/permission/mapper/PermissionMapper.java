package com.example.demo.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.permission.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限数据访问层，提供权限相关查询。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户 ID 查询其已启用角色的权限编码集合。
     *
     * @param userId 用户 ID
     * @return 权限编码列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Select("select distinct code from (" +
            "select p.code as code from demo_system.sys_permission p " +
            "join demo_system.sys_role_permission rp on rp.permission_id = p.id " +
            "join demo_system.sys_role r on r.id = rp.role_id " +
            "join demo_system.sys_user_role ur on ur.role_id = rp.role_id " +
            "where ur.user_id = #{userId} and p.status = 1 and r.status = 1 " +
            "and p.is_deleted = 0 and rp.is_deleted = 0 and r.is_deleted = 0 and ur.is_deleted = 0 " +
            "union " +
            "select m.permission as code from demo_system.sys_menu m " +
            "join demo_system.sys_role_menu rm on rm.menu_id = m.id " +
            "join demo_system.sys_role r2 on r2.id = rm.role_id " +
            "join demo_system.sys_user_role ur2 on ur2.role_id = rm.role_id " +
            "where ur2.user_id = #{userId} and m.status = 1 and r2.status = 1 " +
            "and m.is_deleted = 0 and rm.is_deleted = 0 and r2.is_deleted = 0 and ur2.is_deleted = 0" +
            ") t where t.code is not null and t.code <> ''")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
