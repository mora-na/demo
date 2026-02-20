package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 身份域对外暴露的用户凭据信息（仅用于认证等敏感场景）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
@Data
public class IdentityUserCredentialDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private String password;
}
