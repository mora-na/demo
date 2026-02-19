package com.example.demo.common.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQL 防护配置项，绑定 security.sql-guard 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "security.sql-guard")
public class SqlGuardProperties {

    private boolean enabled = true;

    private boolean blockFullTable = true;

    private boolean blockMultiStatement = true;

    private boolean blockCrossSchemaJoin = true;

    private List<String> allowedSchemas = new ArrayList<>(Arrays.asList(
            "system", "order", "notice", "job", "log", "dict", "cache", "extension", "demo"
    ));

    /**
     * 是否阻止 WITH 语句。
     */
    private boolean blockWithClause = false;

    /**
     * 是否阻止 UNION/UNION ALL。
     */
    private boolean blockUnion = false;

    /**
     * 禁止的函数名列表（大小写不敏感）。
     */
    private List<String> blockedFunctions = new ArrayList<>();

    /**
     * 允许访问的表名白名单（可选，支持 schema.table 或 table）。
     */
    private List<String> allowedTables = new ArrayList<>();

    /**
     * 允许访问的字段白名单（可选，支持 table.column 或 column）。
     */
    private List<String> allowedColumns = new ArrayList<>();

}
