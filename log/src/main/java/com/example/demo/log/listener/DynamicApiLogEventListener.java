package com.example.demo.log.listener;

import com.example.demo.log.api.event.DynamicApiLogEvent;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.entity.SysDynamicApiLog;
import com.example.demo.log.service.SysDynamicApiLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 动态接口日志事件监听器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicApiLogEventListener {

    private final SysDynamicApiLogService logService;
    private final LogConstants logConstants;

    @Async
    @EventListener
    public void handle(DynamicApiLogEvent event) {
        if (event == null) {
            return;
        }
        try {
            SysDynamicApiLog logEntity = new SysDynamicApiLog();
            logEntity.setApiId(event.getApiId());
            logEntity.setApiPath(event.getApiPath());
            logEntity.setApiMethod(event.getApiMethod());
            logEntity.setApiType(event.getApiType());
            logEntity.setAuthMode(event.getAuthMode());
            logEntity.setStatus(event.getStatus());
            logEntity.setResponseCode(event.getResponseCode());
            logEntity.setErrorMsg(event.getErrorMsg());
            logEntity.setTraceId(event.getTraceId());
            logEntity.setUserId(event.getUserId());
            logEntity.setUserName(event.getUserName());
            logEntity.setRequestIp(event.getRequestIp());
            logEntity.setRequestParam(event.getRequestParam());
            logEntity.setDurationMs(event.getDurationMs());
            logEntity.setRequestTime(event.getRequestTime() == null ? LocalDateTime.now() : event.getRequestTime());
            logService.save(logEntity);
        } catch (Exception ex) {
            log.error(logConstants.getMessage().getOperLogPersistFailed(), ex);
        }
    }
}
