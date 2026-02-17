package com.example.demo.log.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 动态接口日志查询参数。
 */
@Data
public class DynamicApiLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long apiId;

    private String apiPath;

    private String apiMethod;

    private Integer status;

    private String userName;

    private String beginTime;

    private String endTime;
}
