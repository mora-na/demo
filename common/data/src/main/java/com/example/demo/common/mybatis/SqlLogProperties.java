package com.example.demo.common.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SQL 日志配置项，绑定 sql-log 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/11
 */
@Data
@Component
@ConfigurationProperties(prefix = "sql-log")
public class SqlLogProperties {

    /**
     * 是否打印 SQL 日志。
     */
    private boolean enabled = false;

}
