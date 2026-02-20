package com.example.demo.identity.listener;

import com.example.demo.identity.api.dto.IdentityUserProfileUpdateRequest;
import com.example.demo.identity.api.event.IdentitySelfProfileUpdateCommand;
import com.example.demo.identity.api.facade.IdentityProfileCommandApi;
import com.example.demo.user.dto.SysUserProfileUpdateRequest;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 身份域跨模块命令监听器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Component
@RequiredArgsConstructor
public class IdentityDomainCommandListener implements IdentityProfileCommandApi {

    private final SysUserService userService;

    @EventListener
    public void handleSelfProfileUpdate(IdentitySelfProfileUpdateCommand command) {
        if (command == null || command.getUserId() == null || command.getRequest() == null) {
            if (command != null) {
                command.markHandled(false);
            }
            return;
        }
        command.markHandled(updateSelfProfile(
                command.getUserId(),
                command.getRequest(),
                command.getNewPassword()
        ));
    }

    @Override
    public boolean updateSelfProfile(Long userId, IdentityUserProfileUpdateRequest request, String newPassword) {
        if (userId == null || request == null) {
            return false;
        }
        return userService.updateSelfProfile(
                userId,
                toSysRequest(request),
                newPassword
        );
    }

    private SysUserProfileUpdateRequest toSysRequest(IdentityUserProfileUpdateRequest request) {
        SysUserProfileUpdateRequest sysRequest = new SysUserProfileUpdateRequest();
        sysRequest.setNickName(request.getNickName());
        sysRequest.setPhone(request.getPhone());
        sysRequest.setEmail(request.getEmail());
        sysRequest.setSex(request.getSex());
        sysRequest.setRemark(request.getRemark());
        return sysRequest;
    }
}
