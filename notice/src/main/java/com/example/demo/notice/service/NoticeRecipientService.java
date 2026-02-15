package com.example.demo.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.notice.entity.NoticeRecipient;

/**
 * 通知接收记录服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface NoticeRecipientService extends IService<NoticeRecipient> {

    java.util.List<Long> listUserIdsByNoticeIds(java.util.List<Long> noticeIds);

    java.util.Map<Long, Long> countUnreadByUserIds(java.util.List<Long> userIds);
}
