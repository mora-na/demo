package com.example.demo.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证上下文中的用户摘要信息，承载鉴权与数据范围字段。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private String userName;
    private String nickName;
    private Long deptId;
    private String dataScopeType;
    private String dataScopeValue;
}
