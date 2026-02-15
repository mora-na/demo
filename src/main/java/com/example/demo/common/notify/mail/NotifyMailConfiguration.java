package com.example.demo.common.notify.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件通知 Bean 装配。
 *
 * <p>规则：
 * <ul>
 *     <li>当 notify.mail.enabled=true 且存在 JavaMailSender 时，启用 SMTP 实现。</li>
 *     <li>其余情况统一回落到 Noop 实现，保证 NotifyMailSender 始终可注入。</li>
 * </ul>
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Slf4j
@Configuration
public class NotifyMailConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "notify.mail", name = "enabled", havingValue = "true")
    @ConditionalOnBean(JavaMailSender.class)
    public NotifyMailSender smtpNotifyMailSender(JavaMailSender javaMailSender,
                                                 MailProperties springMailProperties,
                                                 NotifyMailProperties notifyMailProperties) {
        log.info("notify mail sender enabled: smtp");
        return new SmtpNotifyMailSender(javaMailSender, springMailProperties, notifyMailProperties);
    }

    @Bean
    @ConditionalOnMissingBean(NotifyMailSender.class)
    public NotifyMailSender noopNotifyMailSender() {
        log.info("notify mail sender enabled: noop");
        return new NoopNotifyMailSender();
    }
}
