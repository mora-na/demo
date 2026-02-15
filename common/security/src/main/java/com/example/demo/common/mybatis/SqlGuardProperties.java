package com.example.demo.common.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SQL 防护配置项，绑定 security.sql-guard 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.sql-guard")
public class SqlGuardProperties {

    private boolean enabled = true;

    private boolean blockFullTable = true;

    private boolean blockMultiStatement = true;

}
