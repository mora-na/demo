package com.example.demo.common.web.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * 全局 CORS 配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/20
 */
@Configuration
public class CorsWebMvcConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public CorsWebMvcConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!corsProperties.isEnabled()) {
            return;
        }
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return;
        }

        List<String> pathPatterns = corsProperties.getPathPatterns();
        if (pathPatterns == null || pathPatterns.isEmpty()) {
            pathPatterns = Collections.singletonList("/**");
        }

        String[] origins = allowedOrigins.toArray(new String[0]);
        String[] methods = corsProperties.getAllowedMethods().toArray(new String[0]);
        String[] headers = corsProperties.getAllowedHeaders().toArray(new String[0]);
        String[] exposedHeaders = corsProperties.getExposedHeaders().toArray(new String[0]);

        for (String pattern : pathPatterns) {
            CorsRegistration registration = registry.addMapping(pattern);
            registration.allowedOrigins(origins);
            registration.allowedMethods(methods);
            registration.allowedHeaders(headers);
            if (exposedHeaders.length > 0) {
                registration.exposedHeaders(exposedHeaders);
            }
            registration.allowCredentials(corsProperties.isAllowCredentials());
            registration.maxAge(corsProperties.getMaxAge());
        }
    }
}
