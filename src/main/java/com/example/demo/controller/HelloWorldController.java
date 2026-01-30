package com.example.demo.controller;

import com.example.demo.framework.config.UserConfig;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
public class HelloWorldController {

    private final UserService userService;

    @RequestMapping("/selectUsers")
    public List<UserVO> listUser(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10")int pageSize,
                                 @RequestParam UserVO userVO) {
        return userService.getUserVO(userService.selectUsers(pageNum, pageSize, userVO));
    }


    @GetMapping("/getKeyValue")
    public Object getKeyValue(String key) {
        log.info("Received request to getKeyValue,key:[{}]", key);
        if (StringUtils.isBlank(key)){
            return null;
        }
        Object value = UserConfig.CONFIG.get(key);
        log.info("key:[{}],value:[{}]", key, value);
        return value;
    }

}
