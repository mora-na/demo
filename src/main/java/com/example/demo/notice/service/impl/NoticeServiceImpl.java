package com.example.demo.notice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.notice.dto.*;
import com.example.demo.notice.entity.Notice;
import com.example.demo.notice.entity.NoticeRecipient;
import com.example.demo.notice.mapper.NoticeMapper;
import com.example.demo.notice.mapper.NoticeRecipientMapper;
import com.example.demo.notice.model.NoticeScopeType;
import com.example.demo.notice.service.NoticeRecipientService;
import com.example.demo.notice.service.NoticeService;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final NoticeRecipientMapper noticeRecipientMapper;
    private final NoticeRecipientService noticeRecipientService;
    private final UserService userService;
    private final UserRoleService userRoleService;

    @Override
    public List<Notice> selectNotices(NoticeQuery query) {
        if (query == null) {
            return list(Wrappers.lambdaQuery(Notice.class)
                    .orderByDesc(Notice::getCreatedAt)
                    .orderByDesc(Notice::getId));
        }
        String keyword = StringUtils.trimToEmpty(query.getKeyword());
        String scopeType = StringUtils.trimToEmpty(query.getScopeType());
        return list(Wrappers.lambdaQuery(Notice.class)
                .and(StringUtils.isNotBlank(keyword), wrapper ->
                        wrapper.like(Notice::getTitle, keyword)
                                .or()
                                .like(Notice::getContent, keyword))
                .eq(StringUtils.isNotBlank(scopeType), Notice::getScopeType, scopeType.toUpperCase())
                .orderByDesc(Notice::getCreatedAt)
                .orderByDesc(Notice::getId));
    }

    @Override
    public List<NoticeVO> toNoticeViews(List<Notice> notices) {
        if (notices == null || notices.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> noticeIds = notices.stream()
                .map(Notice::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, NoticeReadStat> stats = new HashMap<>();
        if (!noticeIds.isEmpty()) {
            List<NoticeReadStat> statList = noticeRecipientMapper.selectReadStats(noticeIds);
            if (statList != null) {
                for (NoticeReadStat stat : statList) {
                    if (stat != null && stat.getNoticeId() != null) {
                        stats.put(stat.getNoticeId(), stat);
                    }
                }
            }
        }
        List<NoticeVO> views = new ArrayList<>();
        for (Notice notice : notices) {
            if (notice == null) {
                continue;
            }
            NoticeVO view = new NoticeVO();
            view.setId(notice.getId());
            view.setTitle(notice.getTitle());
            view.setContent(notice.getContent());
            view.setScopeType(notice.getScopeType());
            view.setScopeValue(notice.getScopeValue());
            view.setCreatedBy(notice.getCreatedBy());
            view.setCreatedName(notice.getCreatedName());
            view.setCreatedAt(notice.getCreatedAt());
            NoticeReadStat stat = notice.getId() == null ? null : stats.get(notice.getId());
            view.setTotalCount(stat == null || stat.getTotalCount() == null ? 0L : stat.getTotalCount());
            view.setReadCount(stat == null || stat.getReadCount() == null ? 0L : stat.getReadCount());
            views.add(view);
        }
        return views;
    }

    @Override
    public List<NoticeRecipientVO> listRecipients(Long noticeId) {
        if (noticeId == null) {
            return Collections.emptyList();
        }
        return noticeRecipientMapper.selectRecipientsByNoticeId(noticeId);
    }

    @Override
    public List<NoticeMyVO> listMyNotices(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return noticeRecipientMapper.selectMyNotices(userId);
    }

    @Override
    public long countUnread(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = noticeRecipientMapper.countUnreadByUserId(userId);
        return count == null ? 0L : count;
    }

    @Override
    public boolean markRead(Long noticeId, Long userId) {
        if (noticeId == null || userId == null) {
            return false;
        }
        NoticeRecipient update = new NoticeRecipient();
        update.setReadStatus(NoticeRecipient.STATUS_READ);
        update.setReadTime(LocalDateTime.now());
        return noticeRecipientService.update(update,
                Wrappers.lambdaUpdate(NoticeRecipient.class)
                        .eq(NoticeRecipient::getNoticeId, noticeId)
                        .eq(NoticeRecipient::getUserId, userId)
                        .eq(NoticeRecipient::getReadStatus, NoticeRecipient.STATUS_UNREAD));
    }

    @Override
    public int markAllRead(Long userId) {
        if (userId == null) {
            return 0;
        }
        NoticeRecipient update = new NoticeRecipient();
        update.setReadStatus(NoticeRecipient.STATUS_READ);
        update.setReadTime(LocalDateTime.now());
        return noticeRecipientMapper.update(update,
                Wrappers.lambdaUpdate(NoticeRecipient.class)
                        .eq(NoticeRecipient::getUserId, userId)
                        .eq(NoticeRecipient::getReadStatus, NoticeRecipient.STATUS_UNREAD));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notice publish(NoticePublishRequest request, AuthUser publisher) {
        if (request == null) {
            return null;
        }
        String scopeType = normalizeScopeType(request.getScopeType());
        if (!NoticeScopeType.isSupported(scopeType)) {
            return null;
        }
        List<Long> targetUserIds = resolveTargetUsers(scopeType, request.getScopeIds());
        if (targetUserIds.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        Notice notice = new Notice();
        notice.setTitle(StringUtils.trimToEmpty(request.getTitle()));
        notice.setContent(StringUtils.trimToEmpty(request.getContent()));
        notice.setScopeType(scopeType);
        notice.setScopeValue(buildScopeValue(scopeType, request.getScopeIds()));
        if (publisher != null) {
            notice.setCreatedBy(publisher.getId());
            notice.setCreatedName(StringUtils.defaultIfBlank(publisher.getNickName(), publisher.getUserName()));
        }
        notice.setCreatedAt(now);
        save(notice);

        List<NoticeRecipient> recipients = targetUserIds.stream()
                .map(userId -> new NoticeRecipient(null, notice.getId(), userId, NoticeRecipient.STATUS_UNREAD, null, now))
                .collect(Collectors.toList());
        noticeRecipientService.saveBatch(recipients);
        return notice;
    }

    private String normalizeScopeType(String scopeType) {
        if (scopeType == null) {
            return "";
        }
        return scopeType.trim().toUpperCase(Locale.ROOT);
    }

    private String buildScopeValue(String scopeType, List<Long> scopeIds) {
        if (NoticeScopeType.ALL.equals(scopeType)) {
            return null;
        }
        if (scopeIds == null || scopeIds.isEmpty()) {
            return null;
        }
        return scopeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Long> resolveTargetUsers(String scopeType, List<Long> scopeIds) {
        if (NoticeScopeType.ALL.equals(scopeType)) {
            return listEnabledUserIds(null);
        }
        if (scopeIds == null || scopeIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> uniqueIds = scopeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (NoticeScopeType.DEPT.equals(scopeType)) {
            return listEnabledUserIdsByDept(uniqueIds);
        }
        if (NoticeScopeType.ROLE.equals(scopeType)) {
            return listEnabledUserIdsByRole(uniqueIds);
        }
        if (NoticeScopeType.USER.equals(scopeType)) {
            return listEnabledUserIds(uniqueIds);
        }
        return Collections.emptyList();
    }

    private List<Long> listEnabledUserIdsByDept(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> users = userService.list(Wrappers.lambdaQuery(User.class)
                .in(User::getDeptId, deptIds)
                .eq(User::getStatus, User.STATUS_ENABLED));
        return toIdList(users);
    }

    private List<Long> listEnabledUserIdsByRole(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = userRoleService.list(Wrappers.lambdaQuery(UserRole.class)
                        .in(UserRole::getRoleId, roleIds))
                .stream()
                .map(UserRole::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return listEnabledUserIds(userIds);
    }

    private List<Long> listEnabledUserIds(List<Long> userIds) {
        List<User> users;
        if (userIds == null) {
            users = userService.list(Wrappers.lambdaQuery(User.class)
                    .eq(User::getStatus, User.STATUS_ENABLED));
        } else if (userIds.isEmpty()) {
            return Collections.emptyList();
        } else {
            users = userService.listByIds(userIds);
        }
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .filter(user -> user != null && (user.getStatus() == null || user.getStatus() == User.STATUS_ENABLED))
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> toIdList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
