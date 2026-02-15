package com.example.demo.common.notify.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件通知配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "notify.mail")
public class NotifyMailProperties {

    /**
     * 是否启用 SMTP 邮件发送能力。
     */
    private boolean enabled = false;

    /**
     * 发件人地址，为空时回退到 spring.mail.username。
     */
    private String from = "";

    /**
     * 主题前缀，为空表示不追加。
     */
    private String subjectPrefix = "[Demo]";
}
