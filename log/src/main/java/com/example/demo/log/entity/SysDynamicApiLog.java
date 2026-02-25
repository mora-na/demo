package com.example.demo.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 动态接口日志实体。
 */
@Data
@TableName("demo_log.sys_dynamic_api_log")
public class SysDynamicApiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("api_id")
    private Long apiId;

    @TableField("api_path")
    private String apiPath;

    @TableField("api_method")
    private String apiMethod;

    @TableField("api_type")
    private String apiType;

    @TableField("auth_mode")
    private String authMode;

    @TableField("status")
    private Integer status;

    @TableField("response_code")
    private Integer responseCode;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("error_details")
    private String errorDetails;

    @TableField("meta")
    private String meta;

    @TableField("trace_id")
    private String traceId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("request_ip")
    private String requestIp;

    @TableField("request_param")
    private String requestParam;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField("request_time")
    private LocalDateTime requestTime;
}
