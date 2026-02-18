package com.example.demo.extension.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 动态接口创建请求。
 */
@Data
public class DynamicApiCreateRequest {

    @NotBlank(message = "dynamic.api.path.invalid")
    @Size(max = 256, message = "dynamic.api.path.invalid")
    private String path;

    @NotBlank(message = "dynamic.api.method.invalid")
    @Size(max = 16, message = "dynamic.api.method.invalid")
    private String method;

    @NotBlank(message = "dynamic.api.type.invalid")
    @Size(max = 16, message = "dynamic.api.type.invalid")
    private String type;

    @Size(max = 4000, message = "dynamic.api.config.invalid")
    private String config;

    @Size(max = 16, message = "dynamic.api.config.invalid")
    private String status;

    @Size(max = 16, message = "dynamic.api.config.invalid")
    private String authMode;

    @Size(max = 64, message = "dynamic.api.config.invalid")
    private String rateLimitPolicy;

    private Integer timeoutMs;

    @Size(max = 500, message = "dynamic.api.config.invalid")
    private String remark;

    @Size(max = 128, message = "dynamic.api.config.invalid")
    private String beanName;

    @Size(max = 128, message = "dynamic.api.config.invalid")
    private String paramMode;

    @Size(max = 4000, message = "dynamic.api.config.invalid")
    private String paramSchema;

    @Size(max = 4000, message = "dynamic.api.config.invalid")
    private String sql;

    @Size(max = 512, message = "dynamic.api.config.invalid")
    private String httpUrl;

    @Size(max = 16, message = "dynamic.api.config.invalid")
    private String httpMethod;

    private Boolean httpPassHeaders;

    private Boolean httpPassQuery;
}
