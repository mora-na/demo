package com.example.demo.extension.model;

import lombok.Data;

/**
 * SQL 执行配置。
 */
@Data
public class SqlExecuteConfig {

    /**
     * SQL 文本（仅支持 SELECT）。
     */
    private String sql;
}
