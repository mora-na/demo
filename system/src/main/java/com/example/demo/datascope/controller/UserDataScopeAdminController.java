package com.example.demo.datascope.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.datascope.config.DataScopeConstants;
import com.example.demo.datascope.dto.UserDataScopeQuery;
import com.example.demo.datascope.dto.UserDataScopeVO;
import com.example.demo.datascope.entity.UserDataScope;
import com.example.demo.datascope.service.UserDataScopeService;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.service.MenuService;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户数据范围覆盖集中管理接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Validated
@RestController
@RequestMapping("/user-data-scope")
@RequiredArgsConstructor
public class UserDataScopeAdminController extends BaseController {

    private final UserDataScopeService userDataScopeService;
    private final SysUserService userService;
    private final DeptService deptService;
    private final MenuService menuService;
    private final DataScopeConstants dataScopeConstants;

    @GetMapping("/list")
    @RequirePermission("data-scope:user:query")
    public CommonResult<PageResult<UserDataScopeVO>> list(@ModelAttribute UserDataScopeQuery query) {
        List<Long> userIds = resolveUserIds(query.getUserName());
        List<String> scopeKeys = resolveScopeKeys(query.getMenuKeyword());
        if (userIds != null && userIds.isEmpty()) {
            return success(emptyPage());
        }
        if (scopeKeys != null && scopeKeys.isEmpty()) {
            return success(emptyPage());
        }
        Page<UserDataScope> page = getPageQuery().buildPage();
        IPage<UserDataScope> result = userDataScopeService.page(page, Wrappers.lambdaQuery(UserDataScope.class)
                .in(userIds != null && !userIds.isEmpty(), UserDataScope::getUserId, userIds)
                .in(scopeKeys != null && !scopeKeys.isEmpty(), UserDataScope::getScopeKey, scopeKeys)
                .eq(query.getStatus() != null, UserDataScope::getStatus, query.getStatus())
                .orderByDesc(UserDataScope::getId));
        if (result == null) {
            return success(emptyPage());
        }
        int pageNum = (int) Math.max(result.getCurrent(), 1);
        int pageSize = (int) Math.max(result.getSize(), 1);
        List<UserDataScope> records = result.getRecords() == null ? Collections.emptyList() : result.getRecords();
        if (records.isEmpty()) {
            return success(new PageResult<>(Math.max(result.getTotal(), 0L), Collections.emptyList(), pageNum, pageSize));
        }

        Set<Long> recordUserIds = records.stream()
                .map(UserDataScope::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = recordUserIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(recordUserIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SysUser::getId, user -> user, (a, b) -> a));
        Set<Long> deptIds = userMap.values().stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Dept> deptMap = deptIds.isEmpty()
                ? Collections.emptyMap()
                : deptService.listByIds(deptIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Dept::getId, dept -> dept, (a, b) -> a));

        String globalScopeKey = dataScopeConstants.getScope().getGlobalScopeKey();
        Set<String> recordScopeKeys = records.stream()
                .map(UserDataScope::getScopeKey)
                .filter(StringUtils::isNotBlank)
                .filter(scopeKey -> !globalScopeKey.equals(scopeKey))
                .collect(Collectors.toSet());
        Map<String, Menu> menuMap = recordScopeKeys.isEmpty()
                ? Collections.emptyMap()
                : menuService.list(Wrappers.lambdaQuery(Menu.class).in(Menu::getPermission, recordScopeKeys))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Menu::getPermission, menu -> menu, (a, b) -> a));

        List<UserDataScopeVO> data = records.stream()
                .map(entity -> toVO(entity, userMap, deptMap, menuMap))
                .collect(Collectors.toList());
        return success(new PageResult<>(Math.max(result.getTotal(), 0L), data, pageNum, pageSize));
    }

    private List<Long> resolveUserIds(String userName) {
        if (StringUtils.isBlank(userName)) {
            return null;
        }
        List<SysUser> users = userService.list(Wrappers.lambdaQuery(SysUser.class)
                .like(SysUser::getUserName, userName).or().like(SysUser::getNickName, userName));
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(SysUser::getId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private List<String> resolveScopeKeys(String menuKeyword) {
        if (StringUtils.isBlank(menuKeyword)) {
            return null;
        }
        List<Menu> menus = menuService.list(Wrappers.lambdaQuery(Menu.class)
                .like(Menu::getPermission, menuKeyword).or().like(Menu::getName, menuKeyword));
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        return menus.stream()
                .map(Menu::getPermission)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private UserDataScopeVO toVO(UserDataScope entity,
                                 Map<Long, SysUser> userMap,
                                 Map<Long, Dept> deptMap,
                                 Map<String, Menu> menuMap) {
        if (entity == null) {
            return null;
        }
        UserDataScopeVO vo = new UserDataScopeVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setScopeKey(entity.getScopeKey());
        vo.setDataScopeType(entity.getDataScopeType());
        vo.setDataScopeValue(entity.getDataScopeValue());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());

        SysUser user = entity.getUserId() == null ? null : userMap.get(entity.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setNickName(user.getNickName());
            vo.setDeptId(user.getDeptId());
            Dept dept = user.getDeptId() == null ? null : deptMap.get(user.getDeptId());
            vo.setDeptName(dept == null ? null : dept.getName());
        }

        if (StringUtils.isNotBlank(entity.getScopeKey())
                && !dataScopeConstants.getScope().getGlobalScopeKey().equals(entity.getScopeKey())) {
            Menu menu = menuMap.get(entity.getScopeKey());
            if (menu != null) {
                vo.setMenuName(menu.getName());
                vo.setPermission(menu.getPermission());
            }
        }
        if (dataScopeConstants.getScope().getGlobalScopeKey().equals(entity.getScopeKey())) {
            vo.setMenuName(dataScopeConstants.getScope().getGlobalScopeMenuName());
            vo.setPermission(dataScopeConstants.getScope().getGlobalScopePermission());
        }
        return vo;
    }
}
