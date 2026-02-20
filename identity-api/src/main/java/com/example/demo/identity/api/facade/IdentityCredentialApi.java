package com.example.demo.identity.api.facade;

import com.example.demo.identity.api.dto.IdentityUserCredentialDTO;

/**
 * 身份凭据接口，仅用于认证相关场景。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
public interface IdentityCredentialApi {

    IdentityUserCredentialDTO getUserCredentialById(Long userId);

    IdentityUserCredentialDTO getUserCredentialByUserName(String userName);
}
