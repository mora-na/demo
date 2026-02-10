package com.example.demo.menu.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.menu.dto.MenuCreateRequest;
import com.example.demo.menu.dto.MenuStatusRequest;
import com.example.demo.menu.dto.MenuUpdateRequest;
import com.example.demo.menu.dto.MenuVO;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理后台接口，提供菜单的查询、创建、更新与状态控制。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Validated
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuAdminController extends BaseController {

    private final MenuService menuService;

    /**
     * 获取菜单列表。
     *
     * @return 菜单列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping
    @RequirePermission("menu:query")
    public CommonResult<List<MenuVO>> list() {
        return success(toVOs(menuService.list()));
    }

    /**
     * 查询菜单详情。
     *
     * @param id 菜单 ID
     * @return 菜单详情
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @GetMapping("/{id}")
    @RequirePermission("menu:query")
    public CommonResult<MenuVO> detail(@PathVariable Long id) {
        Menu menu = menuService.getById(id);
        if (menu == null) {
            return error(404, i18n("menu.not.found"));
        }
        return success(toVO(menu));
    }

    /**
     * 创建菜单。
     *
     * @param request 创建请求
     * @return 创建后的菜单信息
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PostMapping
    @RequirePermission("menu:create")
    public CommonResult<MenuVO> create(@Valid @RequestBody MenuCreateRequest request) {
        if (existsCode(request.getCode(), null)) {
            return error(400, i18n("menu.code.exists"));
        }
        if (request.getParentId() != null && menuService.getById(request.getParentId()) == null) {
            return error(400, i18n("menu.parent.not.found"));
        }
        Menu menu = new Menu();
        menu.setName(request.getName());
        menu.setCode(request.getCode());
        menu.setParentId(request.getParentId());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setStatus(normalizeStatus(request.getStatus()));
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setRemark(request.getRemark());
        menuService.save(menu);
        return success(toVO(menu));
    }

    /**
     * 更新菜单基础信息。
     *
     * @param id      菜单 ID
     * @param request 更新请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}")
    @RequirePermission("menu:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody MenuUpdateRequest request) {
        Menu existing = menuService.getById(id);
        if (existing == null) {
            return error(404, i18n("menu.not.found"));
        }
        if (existsCode(request.getCode(), id)) {
            return error(400, i18n("menu.code.exists"));
        }
        if (request.getParentId() != null) {
            if (id.equals(request.getParentId())) {
                return error(400, i18n("menu.parent.cannot.self"));
            }
            if (menuService.getById(request.getParentId()) == null) {
                return error(400, i18n("menu.parent.not.found"));
            }
        }
        Menu menu = new Menu();
        menu.setId(id);
        menu.setName(request.getName());
        menu.setCode(request.getCode());
        menu.setParentId(request.getParentId());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setStatus(request.getStatus());
        menu.setSort(request.getSort());
        menu.setRemark(request.getRemark());
        if (!menuService.updateById(menu)) {
            return error(500, i18n("common.update.failed"));
        }
        return success();
    }

    /**
     * 更新菜单启用状态。
     *
     * @param id      菜单 ID
     * @param request 状态请求
     * @return 更新结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PutMapping("/{id}/status")
    @RequirePermission("menu:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody MenuStatusRequest request) {
        Menu existing = menuService.getById(id);
        if (existing == null) {
            return error(404, i18n("menu.not.found"));
        }
        Integer status = request.getStatus();
        if (notValidStatus(status)) {
            return error(400, i18n("common.status.invalid"));
        }
        if (!menuService.updateStatus(id, status)) {
            return error(500, i18n("common.status.update.failed"));
        }
        return success();
    }

    /**
     * 校验菜单编码是否已存在。
     *
     * @param code      菜单编码
     * @param excludeId 需排除的菜单 ID
     * @return true 表示已存在
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        Menu one = menuService.getOne(Wrappers.lambdaQuery(Menu.class).eq(Menu::getCode, code)
                .ne(excludeId != null, Menu::getId, excludeId));
        return one != null;
    }

    /**
     * 规范化菜单状态，非法值默认回退为启用。
     *
     * @param status 状态值
     * @return 规范化后的状态
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Integer normalizeStatus(Integer status) {
        if (notValidStatus(status)) {
            return 1;
        }
        return status;
    }

    /**
     * 判断状态值是否合法。
     *
     * @param status 状态值
     * @return true 表示非法
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean notValidStatus(Integer status) {
        return status == null || (status != 0 && status != 1);
    }

    /**
     * 转换菜单实体为 VO。
     *
     * @param menu 菜单实体
     * @return 菜单 VO
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private MenuVO toVO(Menu menu) {
        if (menu == null) {
            return new MenuVO();
        }
        MenuVO view = new MenuVO();
        view.setId(menu.getId());
        view.setName(menu.getName());
        view.setCode(menu.getCode());
        view.setParentId(menu.getParentId());
        view.setPath(menu.getPath());
        view.setComponent(menu.getComponent());
        view.setPermission(menu.getPermission());
        view.setStatus(menu.getStatus());
        view.setSort(menu.getSort());
        view.setRemark(menu.getRemark());
        return view;
    }

    /**
     * 批量转换菜单实体为 VO。
     *
     * @param menus 菜单实体列表
     * @return 菜单 VO 列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private List<MenuVO> toVOs(List<Menu> menus) {
        return menus.stream().map(this::toVO).collect(Collectors.toList());
    }
}
