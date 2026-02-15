package com.example.demo.common.notify.mail;

/**
 * 邮件发送抽象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
public interface NotifyMailSender {

    /**
     * 发送纯文本邮件。
     *
     * @param to      收件人
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return true 表示发送成功
     */
    boolean sendText(String to, String subject, String content);
}
