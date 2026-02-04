package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.entity.UserDTO;
import com.example.demo.framework.config.UserConfig;
import com.example.demo.framework.controller.BaseController;
import com.example.demo.framework.web.PageResult;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/update")
    public UserVO update() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("test");
        userDTO.setSex("1");
        return userService.getUserVO(userService.selectOneByMultiId(userDTO));
    }

    @GetMapping("/getKeyValue")
    public Object getKeyValue(String key) {
        log.info("Received request to getKeyValue,key:[{}]", key);
        if (StringUtils.isBlank(key)) {
            return null;
        }
        Object value = UserConfig.CONFIG.get(key);
        log.info("key:[{}],value:[{}]", key, value);
        return value;
    }

}
