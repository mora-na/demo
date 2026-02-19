package com.example.demo.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.menu.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
}
