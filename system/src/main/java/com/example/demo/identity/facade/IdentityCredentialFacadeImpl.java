package com.example.demo.identity.facade;

import com.example.demo.identity.api.dto.IdentityUserCredentialDTO;
import com.example.demo.identity.api.facade.IdentityCredentialApi;
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

    @Override
    public IdentityUserCredentialDTO getUserCredentialById(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = userService.getById(userId);
        return toCredential(user);
    }

    @Override
    public IdentityUserCredentialDTO getUserCredentialByUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return null;
        }
        SysUser user = userService.getByUserName(userName.trim());
        return toCredential(user);
    }

    private IdentityUserCredentialDTO toCredential(SysUser user) {
        if (user == null) {
            return null;
        }
        IdentityUserCredentialDTO dto = new IdentityUserCredentialDTO();
        dto.setUserId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setPassword(user.getPassword());
        return dto;
    }
}
