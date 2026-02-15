package com.example.demo.datascope.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用户级数据范围覆盖配置（Layer3）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScopeOverride implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scopeKey;

    private String dataScopeType;

    private Set<Long> customDeptIds = new LinkedHashSet<>();

    private Integer status;
}
