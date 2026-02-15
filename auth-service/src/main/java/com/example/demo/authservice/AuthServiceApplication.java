package com.example.demo.authservice;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@EnableCaching
@SpringBootApplication(scanBasePackages = "com.example.demo")
@MapperScan({
        "com.example.demo.user.mapper",
        "com.example.demo.dept.mapper",
        "com.example.demo.menu.mapper",
        "com.example.demo.permission.mapper",
        "com.example.demo.post.mapper",
        "com.example.demo.datascope.mapper",
        "com.example.demo.log.mapper",
        "com.example.demo.dict.mapper"
})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        log.info("Auth service started.");
    }
}
