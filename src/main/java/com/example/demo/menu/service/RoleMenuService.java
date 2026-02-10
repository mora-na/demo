package com.example.demo.menu.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.menu.entity.RoleMenu;

import java.util.List;

/**
 * 角色-菜单关联服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface RoleMenuService extends IMppService<RoleMenu> {

    /**
     * 为角色重置并分配菜单集合。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 集合
     * @return true 表示分配成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
}
