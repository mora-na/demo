package com.example.demo.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.tool.ExcelTool;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.user.config.UserConfig;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final UserViewService userViewService;
    private final UserConverter userConverter;
    private final UserConfig userConfig;

    @RequestMapping("/selectUsers1")
    @RequirePermission("user:query")
    public PageResult<UserVO> listUser1(@ModelAttribute UserQuery query) {
        PageResult<User> pageResult = page(() -> userService.selectUsers(query));
        return new PageResult<>(pageResult.getTotal(), userViewService.toViewList(pageResult.getData()), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @RequestMapping("/selectUsers2")
    @RequirePermission("user:query")
    public PageResult<UserVO> listUser2(@ModelAttribute UserQuery query) {
        return page(() -> userService.selectUsers(query), userViewService::toView);
    }

    @RequestMapping("/selectUsers3")
    @RequirePermission("user:query")
    public PageResult<UserVO> listUser3(@ModelAttribute UserQuery query) {
        return page(query, userService::selectUsers, userViewService::toView);
    }

    @RequestMapping("/selectUsers4")
    @RequirePermission("user:query")
    public PageResult<UserVO> listUser4(@ModelAttribute UserQuery query) {
        QueryWrapper<User> wrapper = Wrappers.query(userConverter.toEntity(query));
        return page(wrapper, userService.getBaseMapper()::selectList, userViewService::toView);
    }

    @RequestMapping("/selectUsers5")
    @RequirePermission("user:query")
    public PageResult<UserVO> listUser5(@ModelAttribute UserQuery query) {
        startPage();
        List<User> users = userService.selectUsers(query);
        PageResult<User> pageResult = getPageResult(users);
        return new PageResult<>(pageResult.getTotal(), userViewService.toViewList(pageResult.getData()), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @GetMapping("/getKeyValue")
    @RequirePermission("user:config:read")
    public CommonResult<Object> getKeyValue(String key) {
        log.info("Received request to getKeyValue,key:[{}]", key);
        if (StringUtils.isBlank(key)) {
            return error("key is empty");
        }
        Object value = userConfig.getConfig() == null ? null : userConfig.getConfig().get(key);
        log.info("key:[{}],value:[{}]", key, value);
        return success(value);
    }

    @GetMapping("/export")
    @RequirePermission("user:export")
    public void export(@ModelAttribute UserQuery query, HttpServletResponse response) {
        exportExcel(response, () -> userService.selectUsers(query), User.class, "用户信息" + LocalDateTime.now());
    }

    @PostMapping("/import")
    @RequirePermission("user:import")
    public CommonResult<String> importExcel(@RequestPart("file") MultipartFile file) {
        List<UserVO> userVOList = ExcelTool.importFromMultipart(file, UserVO.class);
        List<User> users = userConverter.toEntityList(userVOList);
        log.info("Received request to importExcel,users:[{}]", users);
        if (userService.saveOrUpdateBatchByMultiField(users)) {
            return success("导入成功！");
        }
        return error("导入失败！");
    }

}
