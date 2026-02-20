package com.example.demo.identity.api.facade;

import com.example.demo.identity.api.dto.IdentityUserProfileUpdateRequest;

/**
 * 身份域资料写接口，仅用于当前用户资料更新等受控场景。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
public interface IdentityProfileCommandApi {

    boolean updateSelfProfile(Long userId, IdentityUserProfileUpdateRequest request, String newPassword);
}
