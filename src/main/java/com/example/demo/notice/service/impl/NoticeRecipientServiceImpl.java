package com.example.demo.notice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.notice.dto.NoticeUnreadCount;
import com.example.demo.notice.entity.NoticeRecipient;
import com.example.demo.notice.mapper.NoticeRecipientMapper;
import com.example.demo.notice.service.NoticeRecipientService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知接收记录服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Service
public class NoticeRecipientServiceImpl extends ServiceImpl<NoticeRecipientMapper, NoticeRecipient>
        implements NoticeRecipientService {

    @Override
    public List<Long> listUserIdsByNoticeIds(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return list(Wrappers.lambdaQuery(NoticeRecipient.class)
                .select(NoticeRecipient::getUserId)
                .in(NoticeRecipient::getNoticeId, noticeIds)
                .eq(NoticeRecipient::getIsDeleted, 0))
                .stream()
                .map(NoticeRecipient::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Long> countUnreadByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<NoticeUnreadCount> counts = getBaseMapper().countUnreadByUserIds(userIds);
        if (counts == null || counts.isEmpty()) {
            return Collections.emptyMap();
        }
        return counts.stream()
                .filter(Objects::nonNull)
                .filter(count -> count.getUserId() != null)
                .collect(Collectors.toMap(
                        NoticeUnreadCount::getUserId,
                        count -> count.getUnreadCount() == null ? 0L : count.getUnreadCount(),
                        (left, right) -> right
                ));
    }
}
