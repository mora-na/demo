package com.example.demo.log.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * User-Agent 解析结果。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class UserAgentInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String browser;
    private String os;
    private String deviceType;
}
