package com.example.demo.log.event;

import java.time.LocalDateTime;

/**
 * 登录日志事件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public class LoginLogEvent {

    private final String userName;
    private final Long userId;
    private final Integer loginType;
    private final Integer status;
    private final String message;
    private final String ip;
    private final String userAgent;
    private final LocalDateTime loginTime;

    public LoginLogEvent(String userName,
                         Long userId,
                         Integer loginType,
                         Integer status,
                         String message,
                         String ip,
                         String userAgent,
                         LocalDateTime loginTime) {
        this.userName = userName;
        this.userId = userId;
        this.loginType = loginType;
        this.status = status;
        this.message = message;
        this.ip = ip;
        this.userAgent = userAgent;
        this.loginTime = loginTime;
    }

    public String getUserName() {
        return userName;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getLoginType() {
        return loginType;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
