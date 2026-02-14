package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@EnableCaching
@SpringBootApplication
@MapperScan({
        "com.example.demo.user.mapper",
        "com.example.demo.dept.mapper",
        "com.example.demo.order.mapper",
        "com.example.demo.menu.mapper",
        "com.example.demo.permission.mapper",
        "com.example.demo.post.mapper",
        "com.example.demo.datascope.mapper",
        "com.example.demo.notice.mapper",
        "com.example.demo.job.mapper"
})
public class DemoApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        log.info("Demo启动成功！");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DemoApplication.class);
    }

}
