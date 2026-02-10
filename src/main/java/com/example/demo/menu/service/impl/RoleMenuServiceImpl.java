package com.example.demo.menu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.menu.entity.RoleMenu;
import com.example.demo.menu.mapper.RoleMenuMapper;
import com.example.demo.menu.service.RoleMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色-菜单关联服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class RoleMenuServiceImpl extends MppServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {

    /**
     * 为角色重置并分配菜单集合。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 集合
     * @return true 表示分配成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            return false;
        }
        remove(Wrappers.lambdaQuery(RoleMenu.class).eq(RoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }
        List<RoleMenu> relations = menuIds.stream()
                .filter(id -> id != null)
                .distinct()
                .map(menuId -> new RoleMenu(null, roleId, menuId))
                .collect(Collectors.toList());
        if (relations.isEmpty()) {
            return true;
        }
        return saveBatch(relations);
    }
}
