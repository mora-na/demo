package com.example.demo.extension.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 动态接口对外数据传输对象。
 */
@Data
public class DynamicApiDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String path;

    private String method;

    private String status;

    private String type;

    private String config;

    private String authMode;

    private String rateLimitPolicy;

    private Integer timeoutMs;

    private Long createBy;

    private Long createDept;

    private Long updateBy;

    private Integer version;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
