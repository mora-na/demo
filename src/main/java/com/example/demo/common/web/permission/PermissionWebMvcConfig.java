package com.example.demo.common.web.permission;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置，注册权限拦截器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
public class PermissionWebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    /**
     * 构造函数，注入权限拦截器。
     *
     * @param permissionInterceptor 权限拦截器
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public PermissionWebMvcConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    /**
     * 注册权限拦截器并应用到所有路径。
     *
     * @param registry 拦截器注册表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
    }
}
