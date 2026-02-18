package com.example.demo.extension.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 动态接口更新命令（全量更新）。
 */
@Data
public class DynamicApiUpdateCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;

    private String method;

    private String type;

    private String config;

    private String status;

    private String authMode;

    private String rateLimitPolicy;

    private Integer timeoutMs;

    private String remark;

    private String beanName;

    private String paramMode;

    private String paramSchema;

    private String sql;

    private String httpUrl;

    private String httpMethod;

    private Boolean httpPassHeaders;

    private Boolean httpPassQuery;
}
