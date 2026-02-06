package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.entity.UserDTO;
import com.example.demo.framework.config.UserConfig;
import com.example.demo.framework.controller.BaseController;
import com.example.demo.framework.tools.ExcelTool;
import com.example.demo.framework.web.CommonResult;
import com.example.demo.framework.web.PageResult;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
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
public class HelloWorldController extends BaseController {

    private final UserService userService;

    @RequestMapping("/selectUsers1")
    public PageResult<UserVO> listUser1(@ModelAttribute UserVO userVO) {
        PageResult<UserDTO> pageResult = page(() -> userService.selectUsers(userVO));
        return new PageResult<>(pageResult.getTotal(), userService.getUserVO(pageResult.getData()), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @RequestMapping("/selectUsers2")
    public PageResult<UserVO> listUser2(@ModelAttribute UserVO userVO) {
        return page(() -> userService.selectUsers(userVO), userService::getUserVO);
    }

    @RequestMapping("/selectUsers3")
    public PageResult<UserVO> listUser3(@ModelAttribute UserVO userVO) {
        return page(userVO, userService::selectUsers, userService::getUserVO);
    }

    @RequestMapping("/selectUsers4")
    public PageResult<UserVO> listUser4(@ModelAttribute UserVO userVO) {

        QueryWrapper<UserDTO> wrapper = Wrappers.query(userService.getUserDTO(userVO));
        return page(wrapper, userService.getBaseMapper()::selectList, userService::getUserVO);
    }

    @RequestMapping("/selectUsers5")
    public PageResult<UserVO> listUser5(@ModelAttribute UserVO userVO) {
        startPage();
        List<UserDTO> userDTOS = userService.selectUsers(userVO);
        PageResult<UserDTO> pageResult = getPageResult(userDTOS);
        return new PageResult<>(pageResult.getTotal(), userService.getUserVO(pageResult.getData()), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @GetMapping("/getKeyValue")
    public CommonResult<Object> getKeyValue(String key) {
        log.info("Received request to getKeyValue,key:[{}]", key);
        if (StringUtils.isBlank(key)) {
            return error("key is empty");
        }
        Object value = UserConfig.CONFIG.get(key);
        log.info("key:[{}],value:[{}]", key, value);
        return success(value);
    }

    @GetMapping("/export")
    public void export(@ModelAttribute UserVO userVO, HttpServletResponse response) {
        List<UserVO> userVOList = userService.getUserVO(userService.selectUsers(userVO));
        exportExcel(response, userVOList, UserVO.class, "用户信息" + LocalDateTime.now());
    }

    @PostMapping("/import")
    public CommonResult<String> importExcel(@RequestPart("file") MultipartFile file) {
        List<UserVO> userVOList = ExcelTool.importFromMultipart(file, UserVO.class);
        List<UserDTO> userDTO = userService.getUserDTO(userVOList);
        log.info("Received request to importExcel,userDTO:[{}]", userDTO);
        if (userService.saveOrUpdateBatchByMultiField(userDTO)) {
            return success("导入成功！");
        }
        return error("导入失败！");
    }

}
