package com.example.demo.extension.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 动态接口创建命令。
 */
@Data
public class DynamicApiCreateCommand implements Serializable {

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
}
