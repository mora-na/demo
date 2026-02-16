package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色摘要信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class IdentityRoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;
}
