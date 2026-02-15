package com.example.demo.notice.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequireLogin;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.notice.config.NoticeConstants;
import com.example.demo.notice.dto.*;
import com.example.demo.notice.entity.Notice;
import com.example.demo.notice.entity.NoticeRecipient;
import com.example.demo.notice.model.NoticeScopeType;
import com.example.demo.notice.service.NoticeRecipientService;
import com.example.demo.notice.service.NoticeService;
import com.example.demo.notice.service.NoticeStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * 系统通知接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Validated
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController extends BaseController {

    private final NoticeService noticeService;
    private final NoticeRecipientService noticeRecipientService;
    private final NoticeStreamService noticeStreamService;
    private final NoticeConstants noticeConstants;

    /**
     * 管理端获取通知列表。
     */
    @GetMapping
    @RequirePermission("notice:query")
    public CommonResult<PageResult<NoticeVO>> list(@ModelAttribute NoticeQuery query) {
        PageResult<Notice> rawPage = page(query, noticeService::selectNoticesPage);
        List<NoticeVO> views = noticeService.toNoticeViews(rawPage.getData());
        PageResult<NoticeVO> result = new PageResult<>(
                rawPage.getTotal(),
                views,
                rawPage.getPageNum(),
                rawPage.getPageSize());
        return success(result);
    }

    /**
     * 发布系统通知。
     */
    @PostMapping
    @RequirePermission("notice:publish")
    public CommonResult<NoticeVO> publish(@Valid @RequestBody NoticePublishRequest request) {
        String scopeType = request.getScopeType();
        if (!NoticeScopeType.isSupported(scopeType)) {
            return error(noticeConstants.getController().getBadRequestCode(),
                    i18n(noticeConstants.getMessage().getNoticeScopeInvalid()));
        }
        if (!NoticeScopeType.all().equalsIgnoreCase(scopeType)) {
            if (request.getScopeIds() == null || request.getScopeIds().isEmpty()) {
                return error(noticeConstants.getController().getBadRequestCode(),
                        i18n(noticeConstants.getMessage().getNoticeScopeEmpty()));
            }
        }
        AuthUser publisher = AuthContext.get();
        Notice notice = noticeService.publish(request, publisher);
        if (notice == null) {
            return error(noticeConstants.getController().getBadRequestCode(),
                    i18n(noticeConstants.getMessage().getNoticeRecipientsEmpty()));
        }
        List<NoticeVO> views = noticeService.toNoticeViews(Collections.singletonList(notice));
        NoticeVO view = views.isEmpty() ? null : views.get(0);
        return success(view);
    }

    /**
     * 管理端获取通知详情。
     */
    @GetMapping("/{id}")
    @RequirePermission("notice:query")
    public CommonResult<NoticeVO> detail(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return error(noticeConstants.getController().getNotFoundCode(),
                    i18n(noticeConstants.getMessage().getNoticeNotFound()));
        }
        List<NoticeVO> views = noticeService.toNoticeViews(Collections.singletonList(notice));
        return success(views.isEmpty() ? null : views.get(0));
    }

    /**
     * 管理端查看通知接收详情。
     */
    @GetMapping("/{id}/recipients")
    @RequirePermission("notice:query")
    public CommonResult<List<NoticeRecipientVO>> recipients(@PathVariable Long id) {
        if (noticeService.getById(id) == null) {
            return error(noticeConstants.getController().getNotFoundCode(),
                    i18n(noticeConstants.getMessage().getNoticeNotFound()));
        }
        return success(noticeService.listRecipients(id));
    }

    /**
     * 用户获取自己的通知列表。
     */
    @GetMapping("/my")
    @RequireLogin
    public CommonResult<PageResult<NoticeMyVO>> myNotices() {
        AuthUser user = AuthContext.get();
        if (user == null || user.getId() == null) {
            return error(noticeConstants.getController().getUnauthorizedCode(),
                    i18n(noticeConstants.getMessage().getAuthPermissionRequired()));
        }
        PageResult<NoticeMyVO> result = page(user.getId(), noticeService::listMyNoticesPage);
        return success(result);
    }

    /**
     * 获取当前用户未读数量。
     */
    @GetMapping("/unread-count")
    @RequireLogin
    public CommonResult<Long> unreadCount() {
        AuthUser user = AuthContext.get();
        if (user == null || user.getId() == null) {
            return error(noticeConstants.getController().getUnauthorizedCode(),
                    i18n(noticeConstants.getMessage().getAuthPermissionRequired()));
        }
        return success(noticeService.countUnread(user.getId()));
    }

    /**
     * 建立通知 SSE 连接，接收新通知推送。
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    @RequireLogin
    public SseEmitter stream() {
        AuthUser user = AuthContext.get();
        if (user == null || user.getId() == null) {
            return new SseEmitter(noticeConstants.getStream().getAnonymousEmitterTimeoutMillis());
        }
        Long userId = user.getId();
        int latestLimit = noticeConstants.getStream().getLatestLimit();
        List<NoticeLatestVO> latestNotices = noticeService.listMyLatestNotices(userId, latestLimit);
        long unreadCount = noticeService.countUnread(userId);
        return noticeStreamService.connect(userId, latestNotices, unreadCount);
    }

    /**
     * 标记单条通知已读。
     */
    @PutMapping("/{id}/read")
    @RequireLogin
    public CommonResult<Void> markRead(@PathVariable Long id) {
        AuthUser user = AuthContext.get();
        if (user == null || user.getId() == null) {
            return error(noticeConstants.getController().getUnauthorizedCode(),
                    i18n(noticeConstants.getMessage().getAuthPermissionRequired()));
        }
        noticeService.markRead(id, user.getId());
        return success();
    }

    /**
     * 一键标记全部已读。
     */
    @PutMapping("/read-all")
    @RequireLogin
    public CommonResult<Integer> markAllRead() {
        AuthUser user = AuthContext.get();
        if (user == null || user.getId() == null) {
            return error(noticeConstants.getController().getUnauthorizedCode(),
                    i18n(noticeConstants.getMessage().getAuthPermissionRequired()));
        }
        int updated = noticeService.markAllRead(user.getId());
        return success(updated);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("notice:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (noticeService.getById(id) == null) {
            return error(noticeConstants.getController().getNotFoundCode(),
                    i18n(noticeConstants.getMessage().getNoticeNotFound()));
        }
        List<Long> affectedUserIds = noticeRecipientService.listUserIdsByNoticeIds(
                java.util.Collections.singletonList(id));
        noticeRecipientService.remove(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(NoticeRecipient.class)
                .eq(NoticeRecipient::getNoticeId, id));
        if (!noticeService.removeById(id)) {
            return error(noticeConstants.getController().getInternalServerErrorCode(),
                    i18n(noticeConstants.getMessage().getCommonDeleteFailed()));
        }
        if (!affectedUserIds.isEmpty()) {
            java.util.Map<Long, Long> unreadCounts = noticeRecipientService.countUnreadByUserIds(affectedUserIds);
            noticeStreamService.pushUnreadCounts(affectedUserIds, unreadCounts,
                    java.util.Collections.singletonList(id));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("notice:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return success();
        }
        List<Long> affectedUserIds = noticeRecipientService.listUserIdsByNoticeIds(uniqueIds);
        noticeRecipientService.remove(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(NoticeRecipient.class)
                .in(NoticeRecipient::getNoticeId, uniqueIds));
        if (!noticeService.removeByIds(uniqueIds)) {
            return error(noticeConstants.getController().getInternalServerErrorCode(),
                    i18n(noticeConstants.getMessage().getCommonDeleteFailed()));
        }
        if (!affectedUserIds.isEmpty()) {
            java.util.Map<Long, Long> unreadCounts = noticeRecipientService.countUnreadByUserIds(affectedUserIds);
            noticeStreamService.pushUnreadCounts(affectedUserIds, unreadCounts, uniqueIds);
        }
        return success();
    }
}
