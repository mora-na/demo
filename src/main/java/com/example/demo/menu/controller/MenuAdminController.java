package com.example.demo.menu.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.menu.config.MenuConstants;
import com.example.demo.menu.dto.MenuCreateRequest;
import com.example.demo.menu.dto.MenuStatusRequest;
import com.example.demo.menu.dto.MenuUpdateRequest;
import com.example.demo.menu.dto.MenuVO;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.entity.RoleMenu;
import com.example.demo.menu.service.MenuService;
import com.example.demo.menu.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
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
    private final RoleMenuService roleMenuService;
    private final MenuConstants menuConstants;

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
            return error(menuConstants.getController().getNotFoundCode(), i18n(menuConstants.getMessage().getMenuNotFound()));
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
            return error(menuConstants.getController().getBadRequestCode(), i18n(menuConstants.getMessage().getMenuCodeExists()));
        }
        if (request.getParentId() != null && menuService.getById(request.getParentId()) == null) {
            return error(menuConstants.getController().getBadRequestCode(), i18n(menuConstants.getMessage().getMenuParentNotFound()));
        }
        Menu menu = new Menu();
        menu.setName(request.getName());
        menu.setCode(request.getCode());
        menu.setParentId(request.getParentId());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setStatus(normalizeStatus(request.getStatus()));
        menu.setSort(request.getSort() == null ? menuConstants.getSort().getDefaultSort() : request.getSort());
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
            return error(menuConstants.getController().getNotFoundCode(), i18n(menuConstants.getMessage().getMenuNotFound()));
        }
        if (existsCode(request.getCode(), id)) {
            return error(menuConstants.getController().getBadRequestCode(), i18n(menuConstants.getMessage().getMenuCodeExists()));
        }
        if (request.getParentId() != null) {
            if (id.equals(request.getParentId())) {
                return error(menuConstants.getController().getBadRequestCode(), i18n(menuConstants.getMessage().getMenuParentCannotSelf()));
            }
            if (menuService.getById(request.getParentId()) == null) {
                return error(menuConstants.getController().getBadRequestCode(), i18n(menuConstants.getMessage().getMenuParentNotFound()));
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
            return error(menuConstants.getController().getInternalServerErrorCode(),
                    i18n(menuConstants.getMessage().getCommonUpdateFailed()));
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
            return error(menuConstants.getController().getNotFoundCode(), i18n(menuConstants.getMessage().getMenuNotFound()));
        }
        Integer status = request.getStatus();
        if (notValidStatus(status)) {
            return error(menuConstants.getController().getBadRequestCode(),
                    i18n(menuConstants.getMessage().getCommonStatusInvalid()));
        }
        if (!menuService.updateStatus(id, status)) {
            return error(menuConstants.getController().getInternalServerErrorCode(),
                    i18n(menuConstants.getMessage().getCommonStatusUpdateFailed()));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("menu:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (menuService.getById(id) == null) {
            return error(menuConstants.getController().getNotFoundCode(), i18n(menuConstants.getMessage().getMenuNotFound()));
        }
        roleMenuService.remove(Wrappers.lambdaQuery(RoleMenu.class).eq(RoleMenu::getMenuId, id));
        if (!menuService.removeById(id)) {
            return error(menuConstants.getController().getInternalServerErrorCode(),
                    i18n(menuConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("menu:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return success();
        }
        roleMenuService.remove(Wrappers.lambdaQuery(RoleMenu.class).in(RoleMenu::getMenuId, uniqueIds));
        if (!menuService.removeByIds(uniqueIds)) {
            return error(menuConstants.getController().getInternalServerErrorCode(),
                    i18n(menuConstants.getMessage().getCommonDeleteFailed()));
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
            return menuConstants.getStatus().getEnabled();
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
        return status == null
                || (status != menuConstants.getStatus().getDisabled()
                && status != menuConstants.getStatus().getEnabled());
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
