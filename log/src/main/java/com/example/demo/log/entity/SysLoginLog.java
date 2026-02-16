package com.example.demo.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
@TableName("log.sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("login_ip")
    private String loginIp;

    @TableField("login_location")
    private String loginLocation;

    @TableField("browser")
    private String browser;

    @TableField("os")
    private String os;

    @TableField("device_type")
    private String deviceType;

    @TableField("login_type")
    private Integer loginType;

    @TableField("status")
    private Integer status;

    @TableField("msg")
    private String msg;

    @TableField("login_time")
    private LocalDateTime loginTime;
}
