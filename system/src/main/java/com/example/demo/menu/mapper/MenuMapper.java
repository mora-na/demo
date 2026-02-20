package com.example.demo.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.menu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询用户可访问的启用菜单（含角色菜单关联）。
     */
    @Select("select distinct m.* from system.sys_menu m " +
            "join system.sys_role_menu rm on rm.menu_id = m.id and rm.is_deleted = 0 " +
            "join system.sys_role r on r.id = rm.role_id and r.is_deleted = 0 " +
            "join system.sys_user_role ur on ur.role_id = r.id and ur.is_deleted = 0 " +
            "where ur.user_id = #{userId} and m.is_deleted = 0 " +
            "and (m.status is null or m.status = 1) " +
            "and (r.status is null or r.status = 1)")
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);
}
