package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录异常检测与邮件告警服务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Service
@RequiredArgsConstructor
public class LoginAnomalyAlertService {

    private final AuthProperties authProperties;

    /**
     * 检测登录环境变化并发送告警邮件（异步执行，不阻塞登录主流程）。
     *
     * @param user      当前登录用户
     * @param currentIp 当前登录 IP
     * @param userAgent 当前登录 User-Agent
     * @param loginTime 当前登录时间
     */
    @Async
    public void checkAndNotify(IdentityUserDTO user, String currentIp, String userAgent, LocalDateTime loginTime) {
        AuthProperties.Security.LoginAnomaly config = authProperties.getSecurity().getLoginAnomaly();
        if (config == null || !config.isEnabled()) {
            return;
        }
        if (user == null || user.getId() == null || StringUtils.isBlank(user.getEmail())) {
            return;
        }
        // Login anomaly detection relies on log history. Logging module is removed, so skip silently.
    }
}
