package com.example.demo.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("dept_id")
    private Long deptId;

    @TableField("dept_name")
    private String deptName;

    @TableField("title")
    private String title;

    @TableField("operation")
    private String operation;

    @TableField("business_type")
    private Integer businessType;

    @TableField("method")
    private String method;

    @TableField("request_method")
    private String requestMethod;

    @TableField("oper_url")
    private String operUrl;

    @TableField("oper_ip")
    private String operIp;

    @TableField("oper_location")
    private String operLocation;

    @TableField("oper_param")
    private String operParam;

    @TableField("oper_result")
    private String operResult;

    @TableField("before_data")
    private String beforeData;

    @TableField("after_data")
    private String afterData;

    @TableField("status")
    private Integer status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("cost_time")
    private Long costTime;

    @TableField("oper_time")
    private LocalDateTime operTime;
}
