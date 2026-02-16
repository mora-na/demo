package com.example.demo.identity.api.event;

import com.example.demo.identity.api.dto.IdentityUserProfileUpdateRequest;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 身份域“当前用户资料更新”命令事件。
 * 用于跨模块状态变更：发布方仅发布命令，身份域负责消费并更新状态。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public class IdentitySelfProfileUpdateCommand {

    private final Long userId;
    private final IdentityUserProfileUpdateRequest request;
    private final String newPassword;
    private final AtomicBoolean handled = new AtomicBoolean(false);
    private final AtomicBoolean updated = new AtomicBoolean(false);

    public IdentitySelfProfileUpdateCommand(Long userId,
                                            IdentityUserProfileUpdateRequest request,
                                            String newPassword) {
        this.userId = userId;
        this.request = request;
        this.newPassword = newPassword;
    }

    public Long getUserId() {
        return userId;
    }

    public IdentityUserProfileUpdateRequest getRequest() {
        return request;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public boolean isHandled() {
        return handled.get();
    }

    public boolean isUpdated() {
        return updated.get();
    }

    public void markHandled(boolean updateResult) {
        this.updated.set(updateResult);
        this.handled.set(true);
    }
}
