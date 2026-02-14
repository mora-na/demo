package com.example.demo.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.tool.ExcelTool;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.user.converter.SysUserConverter;
import com.example.demo.user.dto.SysUserQuery;
import com.example.demo.user.dto.SysUserVO;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import com.example.demo.user.service.SysUserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户示例接口，演示分页查询、配置读取以及 Excel 导入导出能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Slf4j
@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final SysUserService userService;
    private final SysUserViewService userViewService;
    private final SysUserConverter userConverter;

    @RequestMapping("/selectUsers1")
    @RequirePermission("user:query")
    public PageResult<SysUserVO> listUser1(@ModelAttribute SysUserQuery query) {
        PageResult<SysUser> pageResult = page(query, userService::selectUsersPage);
        return new PageResult<>(pageResult.getTotal(), userViewService.toViewList(pageResult.getData()), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @RequestMapping("/selectUsers2")
    @RequirePermission("user:query")
    public PageResult<SysUserVO> listUser2(@ModelAttribute SysUserQuery query) {
        return page(query, userService::selectUsersPage, userViewService::toView);
    }

    @RequestMapping("/selectUsers3")
    @RequirePermission("user:query")
    public PageResult<SysUserVO> listUser3(@ModelAttribute SysUserQuery query) {
        return page(query, userService::selectUsersPage, userViewService::toView);
    }

    @RequestMapping("/selectUsers4")
    @RequirePermission("user:query")
    public PageResult<SysUserVO> listUser4(@ModelAttribute SysUserQuery query) {
        QueryWrapper<SysUser> wrapper = Wrappers.query(userConverter.toEntity(query));
        return page(page -> userService.page(page, wrapper), userViewService::toView);
    }

    @RequestMapping("/selectUsers5")
    @RequirePermission("user:query")
    public PageResult<SysUserVO> listUser5(@ModelAttribute SysUserQuery query) {
        PageResult<SysUser> pageResult = page(query, userService::selectUsersPage);
        return new PageResult<>(pageResult.getTotal(), userViewService.toViewList(pageResult.getData()), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @GetMapping("/export")
    @RequirePermission("user:export")
    public void export(@ModelAttribute SysUserQuery query, HttpServletResponse response) {
        exportExcel(response, page -> userService.selectUsersPage(page, query), SysUser.class, "用户信息" + LocalDateTime.now());
    }

    @PostMapping("/import")
    @RequirePermission("user:import")
    public CommonResult<String> importExcel(@RequestPart("file") MultipartFile file) {
        List<SysUser> users = ExcelTool.importFromMultipart(file, SysUser.class);
        log.info("Received request to importExcel,users:[{}]", users);
        if (userService.saveOrUpdateBatch(users)) {
            return success(i18n("user.import.success"));
        }
        return error(i18n("user.import.failed"));
    }

}
