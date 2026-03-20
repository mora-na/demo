package com.example.demo.common.web.cors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 全局 CORS 配置项，绑定 security.cors 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/20
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /**
     * 是否启用 CORS。
     */
    private boolean enabled = true;

    /**
     * 生效的路径模式。
     */
    private List<String> pathPatterns = new ArrayList<>(Collections.singletonList("/**"));

    /**
     * 允许的来源域名。
     */
    private List<String> allowedOrigins = new ArrayList<>(
            Arrays.asList("http://localhost:8080", "http://127.0.0.1:8080")
    );

    /**
     * 允许的 HTTP 方法。
     */
    private List<String> allowedMethods = new ArrayList<>(
            Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
    );

    /**
     * 允许的请求头。
     */
    private List<String> allowedHeaders = new ArrayList<>(Collections.singletonList("*"));

    /**
     * 暴露给前端的响应头。
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * 是否允许携带 Cookie。
     */
    private boolean allowCredentials = true;

    /**
     * 预检请求缓存时间（秒）。
     */
    @Min(0)
    private long maxAge = 1800;
}
