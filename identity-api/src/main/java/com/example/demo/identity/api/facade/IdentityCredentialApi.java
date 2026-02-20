package com.example.demo.identity.api.facade;

/**
 * 身份凭据接口，仅用于认证相关场景。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
public interface IdentityCredentialApi {

    boolean matchesPasswordById(Long userId, String rawPassword);

    boolean matchesPasswordByUserName(String userName, String rawPassword);
}
