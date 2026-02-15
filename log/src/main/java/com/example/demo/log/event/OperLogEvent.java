package com.example.demo.log.event;

import com.example.demo.log.entity.SysOperLog;

/**
 * 操作日志事件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public class OperLogEvent {

    private final SysOperLog log;

    public OperLogEvent(SysOperLog log) {
        this.log = log;
    }

    public SysOperLog getLog() {
        return log;
    }
}
