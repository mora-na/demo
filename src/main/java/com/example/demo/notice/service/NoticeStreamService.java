package com.example.demo.notice.service;

import com.example.demo.notice.config.NoticeConstants;
import com.example.demo.notice.dto.NoticeLatestVO;
import com.example.demo.notice.dto.NoticePushPayload;
import com.example.demo.notice.entity.Notice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 通知 SSE 流管理，负责建立连接与推送新通知事件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeStreamService {

    private final NoticeConstants noticeConstants;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Deque<NoticeLatestVO>> latestCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService heartbeatExecutor;

    public SseEmitter connect(Long userId) {
        return connect(userId, null, null);
    }

    public SseEmitter connect(Long userId, List<NoticeLatestVO> latestNotices, Long unreadCount) {
        SseEmitter emitter = new SseEmitter(noticeConstants.getStream().getEmitterTimeoutMillis());
        if (userId == null) {
            return emitter;
        }
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((ex) -> removeEmitter(userId, emitter));
        if (latestNotices != null && latestLimit() > 0) {
            latestCache.put(userId, buildLatestDeque(latestNotices));
        }
        sendInitEvent(userId, emitter, unreadCount);
        return emitter;
    }

    public void pushToUsers(Collection<Long> userIds, Notice notice, Map<Long, Long> unreadCounts) {
        if (userIds == null || userIds.isEmpty() || notice == null) {
            return;
        }
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            List<SseEmitter> userEmitters = emitters.get(userId);
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            List<NoticeLatestVO> latestSnapshot = updateLatestCache(userId, notice);
            Long unreadCount = unreadCounts == null
                    ? noticeConstants.getNumeric().getZeroLong()
                    : unreadCounts.getOrDefault(userId, noticeConstants.getNumeric().getZeroLong());
            NoticePushPayload payload = new NoticePushPayload(
                    notice.getId(),
                    notice.getTitle(),
                    notice.getCreatedName(),
                    notice.getCreateTime(),
                    unreadCount == null ? noticeConstants.getNumeric().getZeroLong() : unreadCount,
                    latestSnapshot,
                    null,
                    null
            );
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventNoticeName()).data(payload));
                } catch (IOException ex) {
                    log.debug(noticeConstants.getStream().getLogPushFailed(), userId, ex);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    public void pushUnreadCounts(Collection<Long> userIds, Map<Long, Long> unreadCounts, Collection<Long> removedNoticeIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        Set<Long> removedIds = removedNoticeIds == null || removedNoticeIds.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(removedNoticeIds);
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            List<SseEmitter> userEmitters = emitters.get(userId);
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            List<NoticeLatestVO> latestSnapshot = removedIds.isEmpty()
                    ? latestSnapshot(userId)
                    : removeFromLatestCache(userId, removedIds);
            Long unreadCount = unreadCounts == null
                    ? noticeConstants.getNumeric().getZeroLong()
                    : unreadCounts.getOrDefault(userId, noticeConstants.getNumeric().getZeroLong());
            NoticePushPayload payload = new NoticePushPayload(
                    null,
                    null,
                    null,
                    null,
                    unreadCount == null ? noticeConstants.getNumeric().getZeroLong() : unreadCount,
                    latestSnapshot,
                    null,
                    null
            );
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventNoticeName()).data(payload));
                } catch (IOException ex) {
                    log.debug(noticeConstants.getStream().getLogPushUpdateFailed(), userId, ex);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    @PostConstruct
    public void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, noticeConstants.getStream().getHeartbeatThreadName());
            thread.setDaemon(true);
            return thread;
        });
        long interval = noticeConstants.getStream().getHeartbeatIntervalMillis();
        if (interval <= 0) {
            log.info(noticeConstants.getStream().getLogHeartbeatDisabled(), interval);
            return;
        }
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, interval, interval, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stopHeartbeat() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        if (userId == null || emitter == null) {
            return;
        }
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
            latestCache.remove(userId);
        }
    }

    private void sendInitEvent(Long userId, SseEmitter emitter, Long unreadCount) {
        List<NoticeLatestVO> latestSnapshot = latestLimit() > 0 ? latestSnapshot(userId) : new ArrayList<>();
        Long safeUnreadCount = unreadCount == null ? noticeConstants.getNumeric().getZeroLong() : unreadCount;
        NoticePushPayload payload = new NoticePushPayload(
                null,
                null,
                null,
                null,
                safeUnreadCount,
                latestSnapshot,
                noticeConstants.getStream().getHeartbeatIntervalMillis(),
                noticeConstants.getStream().getHeartbeatTimeoutMillis()
        );
        try {
            emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventInitName()).data(payload));
        } catch (IOException ex) {
            log.debug(noticeConstants.getStream().getLogInitFailed(), userId, ex);
            removeEmitter(userId, emitter);
        }
    }

    private Deque<NoticeLatestVO> buildLatestDeque(List<NoticeLatestVO> latestNotices) {
        int limit = latestLimit();
        if (limit <= 0) {
            return new ArrayDeque<>();
        }
        Deque<NoticeLatestVO> deque = new ArrayDeque<>();
        if (latestNotices != null) {
            for (NoticeLatestVO item : latestNotices) {
                if (item == null) {
                    continue;
                }
                deque.addLast(item);
                if (deque.size() >= limit) {
                    break;
                }
            }
        }
        return deque;
    }

    private List<NoticeLatestVO> updateLatestCache(Long userId, Notice notice) {
        if (userId == null || notice == null) {
            return latestSnapshot(userId);
        }
        int limit = latestLimit();
        if (limit <= 0) {
            return new ArrayList<>();
        }
        Deque<NoticeLatestVO> deque = latestCache.computeIfAbsent(userId, key -> new ArrayDeque<>());
        synchronized (deque) {
            if (notice.getId() != null) {
                deque.removeIf(item -> Objects.equals(item.getId(), notice.getId()));
            }
            NoticeLatestVO latest = new NoticeLatestVO(
                    notice.getId(),
                    notice.getTitle(),
                    notice.getCreatedName(),
                    notice.getCreateTime(),
                    noticeConstants.getRecipient().getUnread(),
                    null
            );
            deque.addFirst(latest);
            while (deque.size() > limit) {
                deque.removeLast();
            }
            return new ArrayList<>(deque);
        }
    }

    private List<NoticeLatestVO> latestSnapshot(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        if (latestLimit() <= 0) {
            return new ArrayList<>();
        }
        Deque<NoticeLatestVO> deque = latestCache.get(userId);
        if (deque == null || deque.isEmpty()) {
            return new ArrayList<>();
        }
        synchronized (deque) {
            return new ArrayList<>(deque);
        }
    }

    private List<NoticeLatestVO> removeFromLatestCache(Long userId, Set<Long> removedNoticeIds) {
        if (userId == null || removedNoticeIds == null || removedNoticeIds.isEmpty()) {
            return latestSnapshot(userId);
        }
        if (latestLimit() <= 0) {
            return new ArrayList<>();
        }
        Deque<NoticeLatestVO> deque = latestCache.get(userId);
        if (deque == null || deque.isEmpty()) {
            return new ArrayList<>();
        }
        synchronized (deque) {
            deque.removeIf(item -> item != null && removedNoticeIds.contains(item.getId()));
            return new ArrayList<>(deque);
        }
    }

    private int latestLimit() {
        return Math.max(noticeConstants.getNumeric().getZeroInt(), noticeConstants.getStream().getLatestLimit());
    }

    private void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            Long userId = entry.getKey();
            List<SseEmitter> userEmitters = entry.getValue();
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventPingName()).data(now));
                } catch (IOException ex) {
                    if (log.isDebugEnabled()) {
                        log.debug(noticeConstants.getStream().getLogHeartbeatFailed(), userId, ex.getMessage());
                    }
                    removeEmitter(userId, emitter);
                }
            }
        }
    }
}
