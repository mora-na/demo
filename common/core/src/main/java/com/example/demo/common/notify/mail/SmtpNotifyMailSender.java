package com.example.demo.common.notify.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * SMTP 邮件发送实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Slf4j
@RequiredArgsConstructor
public class SmtpNotifyMailSender implements NotifyMailSender {

    private final JavaMailSender javaMailSender;
    private final org.springframework.boot.autoconfigure.mail.MailProperties springMailProperties;
    private final NotifyMailProperties notifyMailProperties;

    @Override
    public boolean sendText(String to, String subject, String content) {
        if (StringUtils.isBlank(to) || StringUtils.isBlank(subject) || StringUtils.isBlank(content)) {
            return false;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        String from = resolveFrom();
        if (StringUtils.isNotBlank(from)) {
            message.setFrom(from);
        }
        message.setTo(to.trim());
        message.setSubject(buildSubject(subject));
        message.setText(content);
        try {
            javaMailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.warn("send mail failed, to={}, subject={}", to, subject, ex);
            return false;
        }
    }

    private String resolveFrom() {
        if (StringUtils.isNotBlank(notifyMailProperties.getFrom())) {
            return notifyMailProperties.getFrom().trim();
        }
        return StringUtils.trimToNull(springMailProperties.getUsername());
    }

    private String buildSubject(String subject) {
        String prefix = StringUtils.trimToEmpty(notifyMailProperties.getSubjectPrefix());
        if (StringUtils.isBlank(prefix)) {
            return subject.trim();
        }
        return prefix + " " + subject.trim();
    }
}
