package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("/")
public class HelloWorld {

    private final UserService masterUserService;

    private final UserService slaveUserService;

    public HelloWorld(@Qualifier("masterUserServiceImpl") UserService masterUserService, @Qualifier("slaveUserServiceImpl") UserService slaveUserService) {
        this.masterUserService = masterUserService;
        this.slaveUserService = slaveUserService;
    }

    @RequestMapping("/")
    public String helloWorld() {
        log.info("Received request to helloWorld");
        return "Hello World";
    }

    @GetMapping("/getAllMasterUserInfo")
    public List<User> getAllMasterUserInfo() {
        return masterUserService.getAllUserInfo();
    }

    @GetMapping("/getAllSlaveUserInfo")
    public List<User> getAllSlaveUserInfo() {
        return slaveUserService.getAllUserInfo();
    }

}
