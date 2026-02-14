package com.example.demo.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.notice.dto.*;
import com.example.demo.notice.entity.Notice;

import java.util.List;

/**
 * 通知服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface NoticeService extends IService<Notice> {

    List<Notice> selectNotices(NoticeQuery query);

    IPage<Notice> selectNoticesPage(Page<Notice> page, NoticeQuery query);

    List<NoticeVO> toNoticeViews(List<Notice> notices);

    List<NoticeRecipientVO> listRecipients(Long noticeId);

    List<NoticeMyVO> listMyNotices(Long userId);

    IPage<NoticeMyVO> listMyNoticesPage(Page<NoticeMyVO> page, Long userId);

    List<NoticeLatestVO> listMyLatestNotices(Long userId, int limit);

    long countUnread(Long userId);

    boolean markRead(Long noticeId, Long userId);

    int markAllRead(Long userId);

    Notice publish(NoticePublishRequest request, AuthUser publisher);
}
