package com.example.demo.identity.facade;

import com.example.demo.identity.api.facade.IdentityCredentialApi;
import com.example.demo.identity.api.service.UserPasswordPolicyService;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 身份凭据接口实现，仅暴露认证所需的敏感字段。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class IdentityCredentialFacadeImpl implements IdentityCredentialApi {

    private final SysUserService userService;
    private final UserPasswordPolicyService userPasswordPolicyService;

    @Override
    public boolean matchesPasswordById(Long userId, String rawPassword) {
        if (userId == null || StringUtils.isBlank(rawPassword)) {
            return false;
        }
        SysUser user = userService.getById(userId);
        return matchesPassword(rawPassword, user);
    }

    @Override
    public boolean matchesPasswordByUserName(String userName, String rawPassword) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(rawPassword)) {
            return false;
        }
        SysUser user = userService.getByUserName(userName.trim());
        return matchesPassword(rawPassword, user);
    }

    private boolean matchesPassword(String rawPassword, SysUser user) {
        if (user == null || StringUtils.isBlank(user.getPassword())) {
            return false;
        }
        return userPasswordPolicyService.matches(rawPassword, user.getPassword());
    }
}
