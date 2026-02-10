package com.example.demo.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.menu.entity.Menu;

/**
 * 菜单服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface MenuService extends IService<Menu> {

    /**
     * 更新菜单状态。
     *
     * @param menuId 菜单 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean updateStatus(Long menuId, Integer status);
}
