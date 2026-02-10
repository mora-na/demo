package com.example.demo.menu.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.mapper.MenuMapper;
import com.example.demo.menu.service.MenuService;
import org.springframework.stereotype.Service;

/**
 * 菜单服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class MenuServiceImpl extends MppServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * 更新菜单启用状态。
     *
     * @param menuId 菜单 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean updateStatus(Long menuId, Integer status) {
        if (menuId == null) {
            return false;
        }
        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setStatus(status);
        return updateById(menu);
    }
}
