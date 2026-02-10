package com.example.demo.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 认证模块配置项，绑定 auth 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();

    private Captcha captcha = new Captcha();

    private Password password = new Password();

    private Filter filter = new Filter();

    /**
     * JWT 配置，定义密钥与过期时间。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Data
    public static class Jwt {
        private String secret = "change-me";
        private long ttlSeconds = 7200;
    }

    /**
     * 验证码配置，定义尺寸、长度、过期与清理策略。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Data
    public static class Captcha {
        private int width = 120;
        private int height = 40;
        private int codeLength = 4;
        private int thickness = 2;
        private int expireSeconds = 120;
        private List<String> fontResources = new ArrayList<>();
        private int maxEntries = 10000;
        private int cleanupIntervalSeconds = 60;
    }

    /**
     * 密码处理配置，定义哈希模式与盐值。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Data
    public static class Password {
        private String mode = "bcrypt";
        private String salt = "";
        private String defaultPassword = "";
        private String transportMode = "plain";
        private String transportKey = "";
        private String transportSm2PrivateKey = "";
        private String transportSm2PublicKey = "";
        private boolean strongCheckEnabled = false;
        private int strongMinLength = 8;
        private String strongPattern = "";
    }

    /**
     * 认证过滤器配置，定义启用状态与排除路径。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Data
    public static class Filter {
        private boolean enabled = true;
        private List<String> excludePaths = new ArrayList<>(
                Arrays.asList("/auth/**", "/error")
        );
    }
}
