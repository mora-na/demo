package com.example.demo.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

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

    private LoginLimit loginLimit = new LoginLimit();

    private Filter filter = new Filter();

    private Security security = new Security();

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
        /**
         * 最小密钥长度（字符）。
         */
        private int minSecretLength = 32;
        /**
         * 禁止使用的默认密钥（用于 fail-fast）。
         */
        private String forbiddenSecret = "change-me";
        /**
         * 是否允许通过 query 参数携带 token。
         */
        private boolean allowQueryToken = false;
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
        private double rotateMin = -0.5;
        private double rotateMax = 0.5;
        private double shearXMin = -0.5;
        private double shearXMax = 0.5;
        private double shearYMin = -0.5;
        private double shearYMax = 0.5;
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
        /**
         * 是否在首次登录时强制修改密码。
         */
        private boolean forceChangeOnFirstLogin = true;
        /**
         * 密码过期天数，<=0 表示不启用过期策略。
         */
        private int expireDays = 120;
    }

    /**
     * 登录失败限制配置，定义最大错误次数与锁定时长。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Data
    public static class LoginLimit {
        private boolean enabled = true;
        private int maxErrors = 5;
        private int lockSeconds = 900;
        private int windowSeconds = 900;
        private String keyMode = "user";
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
        private List<String> additionalExcludePaths = new ArrayList<>();

        private static List<String> mergeExcludePaths(List<String> base, List<String> additional) {
            boolean emptyBase = base == null || base.isEmpty();
            boolean emptyAdditional = additional == null || additional.isEmpty();
            if (emptyBase && emptyAdditional) {
                return Collections.emptyList();
            }
            LinkedHashSet<String> merged = new LinkedHashSet<>();
            if (!emptyBase) {
                merged.addAll(base);
            }
            if (!emptyAdditional) {
                merged.addAll(additional);
            }
            return new ArrayList<>(merged);
        }

        public List<String> getExcludePaths() {
            return mergeExcludePaths(excludePaths, additionalExcludePaths);
        }
    }

    /**
     * 认证安全增强配置。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/15
     */
    @Data
    public static class Security {

        private LoginAnomaly loginAnomaly = new LoginAnomaly();

        private OperationConfirm operationConfirm = new OperationConfirm();

        @Data
        public static class LoginAnomaly {
            /**
             * 是否启用异地/设备变更告警。
             */
            private boolean enabled = true;
            /**
             * 是否在 IP 变化时触发告警。
             */
            private boolean notifyOnIpChange = true;
            /**
             * 是否在设备指纹变化时触发告警。
             */
            private boolean notifyOnDeviceChange = true;
            /**
             * 告警邮件主题（不含 notify.mail.subject-prefix）。
             */
            private String mailSubject = "登录安全提醒";
        }

        @Data
        public static class OperationConfirm {
            /**
             * 是否启用敏感操作邮箱二次确认。
             */
            private boolean enabled = true;
            /**
             * 验证码位数。
             */
            private int codeLength = 6;
            /**
             * 验证码有效期（秒）。
             */
            private int codeTtlSeconds = 300;
            /**
             * 发送冷却期（秒）。
             */
            private int resendIntervalSeconds = 60;
            /**
             * 单个验证码最大校验失败次数。
             */
            private int maxVerifyAttempts = 5;
            /**
             * 校验成功后票据有效期（秒）。
             */
            private int ticketTtlSeconds = 900;
            /**
             * 二次确认邮件主题（不含 notify.mail.subject-prefix）。
             */
            private String mailSubject = "敏感操作确认验证码";
        }
    }
}
