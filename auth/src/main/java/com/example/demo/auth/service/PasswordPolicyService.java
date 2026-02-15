package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.system.api.user.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 密码策略服务，统一计算首次登录改密与过期策略。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final AuthProperties authProperties;

    /**
     * 是否启用首次登录强制修改密码。
     */
    public boolean isForceChangeOnFirstLoginEnabled() {
        return authProperties.getPassword().isForceChangeOnFirstLogin();
    }

    /**
     * 密码过期天数，<=0 表示不启用过期策略。
     */
    public int getExpireDays() {
        return authProperties.getPassword().getExpireDays();
    }

    /**
     * 当前用户是否命中首次登录强制改密。
     */
    public boolean isFirstLoginForceChange(UserAccountDTO user) {
        if (!isForceChangeOnFirstLoginEnabled() || user == null) {
            return false;
        }
        return user.getForcePasswordChange() != null
                && user.getForcePasswordChange().equals(UserAccountDTO.FORCE_PASSWORD_CHANGE_YES);
    }

    /**
     * 当前用户密码是否已过期。
     */
    public boolean isPasswordExpired(UserAccountDTO user) {
        if (user == null) {
            return false;
        }
        int expireDays = getExpireDays();
        if (expireDays <= 0) {
            return false;
        }
        LocalDateTime updatedAt = user.getPasswordUpdatedAt();
        if (updatedAt == null) {
            return false;
        }
        LocalDateTime expireAt = updatedAt.plusDays(expireDays);
        return !expireAt.isAfter(LocalDateTime.now());
    }

    /**
     * 是否需要强制修改密码（首次登录或密码过期）。
     */
    public boolean isPasswordChangeRequired(UserAccountDTO user) {
        return isFirstLoginForceChange(user) || isPasswordExpired(user);
    }
}
