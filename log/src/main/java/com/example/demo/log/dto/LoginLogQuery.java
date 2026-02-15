package com.example.demo.log.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录日志查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class LoginLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String loginIp;

    private Integer status;

    private Integer loginType;

    private String beginTime;

    private String endTime;
}
