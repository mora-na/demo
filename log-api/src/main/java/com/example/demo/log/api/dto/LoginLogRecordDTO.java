package com.example.demo.log.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志只读记录。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class LoginLogRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String loginIp;
    private String browser;
    private String os;
    private String deviceType;
    private LocalDateTime loginTime;
}
