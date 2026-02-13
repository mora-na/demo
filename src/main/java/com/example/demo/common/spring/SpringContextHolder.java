package com.example.demo.common.spring;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文持有器，便于在非 Spring 托管对象中获取 Bean。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> type) {
        if (context == null) {
            return null;
        }
        return context.getBean(type);
    }

    public static Object getBean(String name) {
        if (context == null) {
            return null;
        }
        return context.getBean(name);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        context = applicationContext;
    }
}
