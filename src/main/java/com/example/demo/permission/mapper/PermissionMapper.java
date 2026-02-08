package com.example.demo.permission.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper {

    @Select("select p.code from sys_permission p " +
            "join sys_user_permission up on up.permission_id = p.id " +
            "where up.user_id = #{userId}")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
