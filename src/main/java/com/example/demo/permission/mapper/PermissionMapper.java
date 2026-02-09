package com.example.demo.permission.mapper;

import com.example.demo.common.mybatis.MppBaseMapper;
import com.example.demo.permission.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends MppBaseMapper<Permission> {

    @Select("select distinct p.code from sys_permission p " +
            "join sys_role_permission rp on rp.permission_id = p.id " +
            "join sys_role r on r.id = rp.role_id " +
            "join sys_user_role ur on ur.role_id = rp.role_id " +
            "where ur.user_id = #{userId} and p.status = 1 and r.status = 1")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
