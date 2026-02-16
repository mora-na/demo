package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.common.notify.mail.NotifyMailSender;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.example.demo.log.api.dto.LoginLogRecordDTO;
import com.example.demo.log.api.dto.UserAgentInfoDTO;
import com.example.demo.log.api.facade.LoginLogReadFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 登录异常检测与邮件告警服务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAnomalyAlertService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuthProperties authProperties;
    private final AuthConstants authConstants;
    private final LoginLogReadFacade loginLogReadFacade;
    private final NotifyMailSender notifyMailSender;

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
        LoginLogRecordDTO previous = queryLastSuccessLogin(user.getId());
        if (previous == null) {
            return;
        }
        UserAgentInfoDTO currentUa = loginLogReadFacade.parseUserAgent(userAgent);
        boolean ipChanged = isIpChanged(previous.getLoginIp(), currentIp);
        boolean deviceChanged = isDeviceChanged(previous, currentUa);
        boolean alert = (config.isNotifyOnIpChange() && ipChanged)
                || (config.isNotifyOnDeviceChange() && deviceChanged);
        if (!alert) {
            return;
        }
        String subject = StringUtils.defaultIfBlank(config.getMailSubject(), "登录安全提醒");
        String content = buildMailContent(user, previous, currentIp, currentUa, ipChanged, deviceChanged, loginTime);
        boolean sent = notifyMailSender.sendText(user.getEmail(), subject, content);
        if (!sent) {
            log.warn(authConstants.getSecurity().getLoginAnomaly().getMailSendFailedLogTemplate(),
                    user.getId(), user.getEmail());
        }
    }

    private LoginLogRecordDTO queryLastSuccessLogin(Long userId) {
        AuthConstants.LoginLog loginLogConstants = authConstants.getLoginLog();
        return loginLogReadFacade.getLatestByUserAndStatus(
                userId,
                loginLogConstants.getTypeLogin(),
                loginLogConstants.getStatusSuccess()
        );
    }

    private boolean isIpChanged(String previousIp, String currentIp) {
        if (StringUtils.isBlank(previousIp) || StringUtils.isBlank(currentIp)) {
            return false;
        }
        return !StringUtils.equals(previousIp.trim(), currentIp.trim());
    }

    private boolean isDeviceChanged(LoginLogRecordDTO previous, UserAgentInfoDTO currentUa) {
        String previousFingerprint = buildDeviceFingerprint(
                previous == null ? null : previous.getDeviceType(),
                previous == null ? null : previous.getOs(),
                previous == null ? null : previous.getBrowser()
        );
        String currentFingerprint = buildDeviceFingerprint(
                currentUa == null ? null : currentUa.getDeviceType(),
                currentUa == null ? null : currentUa.getOs(),
                currentUa == null ? null : currentUa.getBrowser()
        );
        if (StringUtils.isBlank(previousFingerprint) || StringUtils.isBlank(currentFingerprint)) {
            return false;
        }
        return !StringUtils.equalsIgnoreCase(previousFingerprint, currentFingerprint);
    }

    private String buildDeviceFingerprint(String deviceType, String os, String browser) {
        String unknown = authConstants.getSecurity().getLoginAnomaly().getUnknownValue();
        List<String> parts = new ArrayList<>();
        addFingerprintPart(parts, deviceType, unknown);
        addFingerprintPart(parts, os, unknown);
        addFingerprintPart(parts, browser, unknown);
        if (parts.isEmpty()) {
            return "";
        }
        return String.join(authConstants.getSecurity().getLoginAnomaly().getDeviceSeparator(), parts);
    }

    private void addFingerprintPart(List<String> parts, String value, String unknown) {
        if (parts == null) {
            return;
        }
        String normalized = StringUtils.trimToEmpty(value);
        if (StringUtils.isBlank(normalized)) {
            return;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (StringUtils.isNotBlank(unknown) && lower.equals(unknown.toLowerCase(Locale.ROOT))) {
            return;
        }
        if ("unknown".equals(lower) || "未知".equals(lower)) {
            return;
        }
        parts.add(normalized);
    }

    private String buildMailContent(IdentityUserDTO user,
                                    LoginLogRecordDTO previous,
                                    String currentIp,
                                    UserAgentInfoDTO currentUa,
                                    boolean ipChanged,
                                    boolean deviceChanged,
                                    LocalDateTime loginTime) {
        String unknown = authConstants.getSecurity().getLoginAnomaly().getUnknownValue();
        String previousIp = normalize(previous == null ? null : previous.getLoginIp(), unknown);
        String currentIpText = normalize(currentIp, unknown);
        String previousDevice = buildDeviceFingerprint(
                previous == null ? null : previous.getDeviceType(),
                previous == null ? null : previous.getOs(),
                previous == null ? null : previous.getBrowser()
        );
        String currentDevice = buildDeviceFingerprint(
                currentUa == null ? null : currentUa.getDeviceType(),
                currentUa == null ? null : currentUa.getOs(),
                currentUa == null ? null : currentUa.getBrowser()
        );
        String previousTime = previous == null ? unknown : formatTime(previous.getLoginTime(), unknown);
        String currentTime = formatTime(loginTime, unknown);
        StringBuilder builder = new StringBuilder(256);
        builder.append("系统检测到账号登录环境发生变化，请确认是否为本人操作。").append('\n')
                .append('\n')
                .append("账号：").append(normalize(user == null ? null : user.getUserName(), unknown)).append('\n')
                .append("本次登录时间：").append(currentTime).append('\n')
                .append("本次登录IP：").append(currentIpText).append('\n')
                .append("本次设备：").append(normalize(currentDevice, unknown)).append('\n')
                .append('\n')
                .append("上次成功登录时间：").append(previousTime).append('\n')
                .append("上次成功登录IP：").append(previousIp).append('\n')
                .append("上次设备：").append(normalize(previousDevice, unknown)).append('\n')
                .append('\n')
                .append("变化项：");
        if (ipChanged && deviceChanged) {
            builder.append("IP、设备");
        } else if (ipChanged) {
            builder.append("IP");
        } else if (deviceChanged) {
            builder.append("设备");
        } else {
            builder.append("无");
        }
        builder.append('\n')
                .append('\n')
                .append("若非本人操作，请立即修改密码并检查账号安全。");
        return builder.toString();
    }

    private String normalize(String value, String fallback) {
        String normalized = StringUtils.trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private String formatTime(LocalDateTime time, String fallback) {
        if (time == null) {
            return fallback;
        }
        return TIME_FORMATTER.format(time);
    }
}
