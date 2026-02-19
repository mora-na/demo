package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.support.ClientIpResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 验证码限流 Key 解析器。
 */
@Component
public class CaptchaRateLimitKeyResolver {

    private final AuthProperties authProperties;
    private final ClientIpResolver clientIpResolver;

    public CaptchaRateLimitKeyResolver(AuthProperties authProperties,
                                       ClientIpResolver clientIpResolver) {
        this.authProperties = authProperties;
        this.clientIpResolver = clientIpResolver;
    }

    public String resolve(HttpServletRequest request) {
        AuthProperties.Captcha captcha = authProperties.getCaptcha();
        String keyMode = captcha == null ? null : captcha.getRateLimitKeyMode();
        String mode = keyMode == null ? "ip" : keyMode.trim().toLowerCase();
        if ("global".equals(mode)) {
            return "global";
        }
        String ip = clientIpResolver.resolve(request);
        if ("ip".equals(mode)) {
            return StringUtils.isBlank(ip) ? "global" : ip;
        }
        if ("ip-ua".equals(mode)) {
            String ua = request == null ? null : StringUtils.trimToNull(request.getHeader("User-Agent"));
            String ipPart = StringUtils.isBlank(ip) ? "global" : ip;
            String uaHash = hashUa(ua);
            return uaHash == null ? ipPart : ipPart + ":" + uaHash;
        }
        return "global";
    }

    private String hashUa(String ua) {
        if (StringUtils.isBlank(ua)) {
            return null;
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(ua.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(16);
            for (int i = 0; i < 8 && i < bytes.length; i++) {
                builder.append(String.format("%02x", bytes[i]));
            }
            return builder.toString();
        } catch (Exception ex) {
            return null;
        }
    }
}
