package com.example.demo.log.listener;

import com.example.demo.log.api.event.LoginLogEvent;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.entity.SysLoginLog;
import com.example.demo.log.service.SysLoginLogService;
import com.example.demo.log.support.IpUtils;
import com.example.demo.log.support.UserAgentUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录日志事件监听器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogEventListener {

    private final SysLoginLogService loginLogService;
    private final LogConstants logConstants;

    @Async
    @EventListener
    public void handle(LoginLogEvent event) {
        if (event == null) {
            return;
        }
        try {
            SysLoginLog logEntity = new SysLoginLog();
            logEntity.setUserId(event.getUserId());
            logEntity.setUserName(event.getUserName());
            logEntity.setLoginType(event.getLoginType());
            logEntity.setStatus(event.getStatus());
            logEntity.setMsg(event.getMessage());
            logEntity.setLoginTime(event.getLoginTime() == null ? LocalDateTime.now() : event.getLoginTime());

            String ip = event.getIp();
            if (StringUtils.isNotBlank(ip)) {
                logEntity.setLoginIp(ip);
                logEntity.setLoginLocation(IpUtils.resolveLocation(ip));
            }

            UserAgentUtils.UserAgentInfo info = UserAgentUtils.parse(event.getUserAgent());
            logEntity.setBrowser(info.getBrowser());
            logEntity.setOs(info.getOs());
            logEntity.setDeviceType(info.getDeviceType());

            loginLogService.save(logEntity);
        } catch (Exception e) {
            log.error(logConstants.getMessage().getLoginLogPersistFailed(), e);
        }
    }
}
