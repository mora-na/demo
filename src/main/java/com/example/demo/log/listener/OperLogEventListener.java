package com.example.demo.log.listener;

import com.example.demo.log.entity.SysOperLog;
import com.example.demo.log.event.OperLogEvent;
import com.example.demo.log.service.SysOperLogService;
import com.example.demo.log.support.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogEventListener {

    private final SysOperLogService operLogService;

    @Async
    @EventListener
    public void handle(OperLogEvent event) {
        if (event == null || event.getLog() == null) {
            return;
        }
        try {
            SysOperLog logEntity = event.getLog();
            if (StringUtils.isNotBlank(logEntity.getOperIp())) {
                logEntity.setOperLocation(IpUtils.resolveLocation(logEntity.getOperIp()));
            }
            operLogService.save(logEntity);
        } catch (Exception e) {
            log.error("操作日志入库失败", e);
        }
    }
}
