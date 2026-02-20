package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用户级数据范围覆盖 DTO（身份域对外契约）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
@Data
public class IdentityUserScopeOverrideDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scopeKey;

    private String dataScopeType;

    private Set<Long> customDeptIds = new LinkedHashSet<>();

    private Integer status;
}
