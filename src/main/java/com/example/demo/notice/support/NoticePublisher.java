package com.example.demo.notice.support;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.notice.dto.NoticePublishRequest;
import com.example.demo.notice.entity.Notice;
import com.example.demo.notice.model.NoticeScopeType;
import com.example.demo.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知发布工具，便于业务模块主动触发通知。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Component
@RequiredArgsConstructor
public class NoticePublisher {

    private final NoticeService noticeService;

    public Notice publishToAll(String title, String content) {
        return publish(title, content, NoticeScopeType.ALL, null, AuthContext.get());
    }

    public Notice publishToDepts(String title, String content, List<Long> deptIds) {
        return publish(title, content, NoticeScopeType.DEPT, deptIds, AuthContext.get());
    }

    public Notice publishToRoles(String title, String content, List<Long> roleIds) {
        return publish(title, content, NoticeScopeType.ROLE, roleIds, AuthContext.get());
    }

    public Notice publishToUsers(String title, String content, List<Long> userIds) {
        return publish(title, content, NoticeScopeType.USER, userIds, AuthContext.get());
    }

    public Notice publishToAll(String title, String content, AuthUser publisher) {
        return publish(title, content, NoticeScopeType.ALL, null, publisher);
    }

    public Notice publishToDepts(String title, String content, List<Long> deptIds, AuthUser publisher) {
        return publish(title, content, NoticeScopeType.DEPT, deptIds, publisher);
    }

    public Notice publishToRoles(String title, String content, List<Long> roleIds, AuthUser publisher) {
        return publish(title, content, NoticeScopeType.ROLE, roleIds, publisher);
    }

    public Notice publishToUsers(String title, String content, List<Long> userIds, AuthUser publisher) {
        return publish(title, content, NoticeScopeType.USER, userIds, publisher);
    }

    private Notice publish(String title, String content, String scopeType, List<Long> scopeIds, AuthUser publisher) {
        NoticePublishRequest request = new NoticePublishRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setScopeType(scopeType);
        request.setScopeIds(scopeIds);
        return noticeService.publish(request, publisher);
    }
}
