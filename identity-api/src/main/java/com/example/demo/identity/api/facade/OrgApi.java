package com.example.demo.identity.api.facade;

import org.springframework.lang.Nullable;

/**
 * 组织接口，提供组织树相关查询能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public interface OrgApi {

    @Nullable
    String getDeptNameById(Long deptId);
}
