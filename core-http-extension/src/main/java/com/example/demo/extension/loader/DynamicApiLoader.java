package com.example.demo.extension.loader;

import com.example.demo.extension.manager.DynamicApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 动态接口启动加载器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicApiLoader implements ApplicationRunner {

    private final DynamicApiService dynamicApiService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            dynamicApiService.reloadAll();
            log.info("Dynamic API registry loaded.");
        } catch (Exception ex) {
            log.warn("Dynamic API registry load failed", ex);
        }
    }
}
