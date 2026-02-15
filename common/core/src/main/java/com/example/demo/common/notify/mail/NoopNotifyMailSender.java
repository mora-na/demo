package com.example.demo.common.notify.mail;

import lombok.extern.slf4j.Slf4j;

/**
 * 未启用邮件时的空实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Slf4j
public class NoopNotifyMailSender implements NotifyMailSender {

    @Override
    public boolean sendText(String to, String subject, String content) {
        log.warn("mail sender disabled, skip send: to={}, subject={}", to, subject);
        return false;
    }
}
