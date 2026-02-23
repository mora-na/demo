package com.example.demo.notice.service;

import com.example.demo.notice.config.NoticeConstants;
import com.example.demo.notice.dto.NoticeLatestVO;
import com.example.demo.notice.dto.NoticePushPayload;
import com.example.demo.notice.dto.NoticeStreamMetricsVO;
import com.example.demo.notice.entity.Notice;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final AtomicInteger totalConnections = new AtomicInteger();
    private Cache<Long, Deque<NoticeLatestVO>> latestCache;
    private Cache<Long, AtomicInteger> userConnectionCounter;
    private ScheduledExecutorService heartbeatExecutor;

    public SseEmitter connect(Long userId) {
        return connect(userId, null, null);
    }

    public SseEmitter connect(Long userId, List<NoticeLatestVO> latestNotices, Long unreadCount) {
        SseEmitter emitter = new SseEmitter(resolveEmitterTimeoutMillis());
        if (userId == null) {
            return emitter;
        }
        boolean replaced = closeUserEmitters(userId);
        if (!replaced && !tryAcquireConnection(userId)) {
            return rejectConnection(userId);
        }
        if (replaced) {
            acquireConnection(userId);
        }
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError((ex) -> removeEmitter(userId, emitter));
        if (latestNotices != null && isLatestCacheEnabled()) {
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
                    null,
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
                    null,
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

    public NoticeStreamMetricsVO snapshotMetrics() {
        NoticeStreamMetricsVO metrics = new NoticeStreamMetricsVO();
        metrics.setTotalConnections(Math.max(0, totalConnections.get()));
        metrics.setActiveUsers(emitters.size());
        metrics.setLatestCacheSize(latestCache == null ? 0 : latestCache.estimatedSize());
        metrics.setConnectionCounterSize(userConnectionCounter == null ? 0 : userConnectionCounter.estimatedSize());
        metrics.setLatestLimit(latestLimit());
        metrics.setMaxTotalConnections(noticeConstants.getStream().getMaxTotalConnections());
        metrics.setMaxConnectionsPerUser(noticeConstants.getStream().getMaxConnectionsPerUser());
        metrics.setLatestCacheMaxSize(noticeConstants.getStream().getLatestCacheMaxSize());
        metrics.setLatestCacheExpireMinutes(noticeConstants.getStream().getLatestCacheExpireMinutes());
        metrics.setAutoDegradeEnabled(noticeConstants.getStream().isAutoDegradeEnabled());
        metrics.setDegraded(isDegraded());
        metrics.setDegradeConnectionRatio(resolveDegradeConnectionRatio());
        metrics.setDegradeCacheRatio(resolveDegradeCacheRatio());
        return metrics;
    }

    @PostConstruct
    public void startHeartbeat() {
        initCaches();
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
        if (!userEmitters.remove(emitter)) {
            return;
        }
        releaseConnection(userId);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
            if (latestCache != null) {
                latestCache.invalidate(userId);
            }
        }
    }

    private boolean closeUserEmitters(Long userId) {
        if (userId == null) {
            return false;
        }
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return false;
        }
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.complete();
            } catch (RuntimeException ex) {
                log.debug("Failed to close existing notice SSE emitter for user {}.", userId, ex);
                removeEmitter(userId, emitter);
            }
        }
        return true;
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
                noticeConstants.getStream().getHeartbeatTimeoutMillis(),
                resolveRetryAfterMillis(),
                null
        );
        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name(noticeConstants.getStream().getEventInitName())
                    .data(payload);
            long retryAfterMillis = resolveRetryAfterMillis();
            if (retryAfterMillis > 0) {
                builder.reconnectTime(retryAfterMillis);
            }
            emitter.send(builder);
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
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        int limit = latestLimit();
        Deque<NoticeLatestVO> deque = latestCache.get(userId, key -> new ArrayDeque<>());
        synchronized (Objects.requireNonNull(deque)) {
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
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        Deque<NoticeLatestVO> deque = latestCache == null ? null : latestCache.getIfPresent(userId);
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
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        Deque<NoticeLatestVO> deque = latestCache == null ? null : latestCache.getIfPresent(userId);
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

    private long resolveEmitterTimeoutMillis() {
        long timeout = noticeConstants.getStream().getEmitterTimeoutMillis();
        if (timeout <= 0) {
            return 0L;
        }
        return timeout;
    }

    private long resolveRetryAfterMillis() {
        long retryAfterMillis = noticeConstants.getStream().getRetryAfterMillis();
        if (retryAfterMillis <= 0) {
            return 0L;
        }
        return retryAfterMillis;
    }

    private void initCaches() {
        this.latestCache = buildLatestCache();
        this.userConnectionCounter = buildUserConnectionCounter();
    }

    private Cache<Long, Deque<NoticeLatestVO>> buildLatestCache() {
        NoticeConstants.Stream stream = noticeConstants.getStream();
        long maxSize = stream.getLatestCacheMaxSize();
        int expireMinutes = stream.getLatestCacheExpireMinutes();
        if (maxSize <= 0 || expireMinutes <= 0) {
            return Caffeine.newBuilder()
                    .maximumSize(1)
                    .expireAfterAccess(Duration.ofSeconds(1))
                    .build();
        }
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(Duration.ofMinutes(expireMinutes))
                .build();
    }

    private Cache<Long, AtomicInteger> buildUserConnectionCounter() {
        NoticeConstants.Stream stream = noticeConstants.getStream();
        int maxSize = Math.max(1, stream.getConnectionCounterMaxSize());
        int expireMinutes = Math.max(1, stream.getConnectionCounterExpireMinutes());
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(Duration.ofMinutes(expireMinutes))
                .build();
    }

    private boolean isLatestCacheEnabled() {
        return latestLimit() > 0
                && noticeConstants.getStream().getLatestCacheMaxSize() > 0
                && !isDegraded();
    }

    private boolean isDegraded() {
        NoticeConstants.Stream stream = noticeConstants.getStream();
        if (!stream.isAutoDegradeEnabled()) {
            return false;
        }
        int maxTotal = stream.getMaxTotalConnections();
        if (maxTotal > 0) {
            double ratio = resolveDegradeConnectionRatio();
            if (ratio > 0 && totalConnections.get() >= Math.ceil(maxTotal * ratio)) {
                return true;
            }
        }
        long maxCacheSize = stream.getLatestCacheMaxSize();
        if (maxCacheSize > 0 && latestCache != null) {
            double ratio = resolveDegradeCacheRatio();
            if (ratio > 0 && latestCache.estimatedSize() >= Math.ceil(maxCacheSize * ratio)) {
                return true;
            }
        }
        return false;
    }

    private double resolveDegradeConnectionRatio() {
        double ratio = noticeConstants.getStream().getDegradeConnectionRatio();
        return ratio <= 0 ? 0d : Math.min(1d, ratio);
    }

    private double resolveDegradeCacheRatio() {
        double ratio = noticeConstants.getStream().getDegradeCacheRatio();
        return ratio <= 0 ? 0d : Math.min(1d, ratio);
    }

    private boolean tryAcquireConnection(Long userId) {
        if (userId == null) {
            return true;
        }
        int maxTotal = noticeConstants.getStream().getMaxTotalConnections();
        int current = totalConnections.incrementAndGet();
        if (maxTotal > 0 && current > maxTotal) {
            totalConnections.decrementAndGet();
            return false;
        }
        int maxPerUser = noticeConstants.getStream().getMaxConnectionsPerUser();
        if (maxPerUser > 0) {
            AtomicInteger counter = userConnectionCounter.get(userId, key -> new AtomicInteger());
            if (counter != null && counter.incrementAndGet() > maxPerUser) {
                counter.decrementAndGet();
                totalConnections.decrementAndGet();
                return false;
            }
        }
        return true;
    }

    private void acquireConnection(Long userId) {
        if (userId == null) {
            return;
        }
        totalConnections.incrementAndGet();
        int maxPerUser = noticeConstants.getStream().getMaxConnectionsPerUser();
        if (maxPerUser > 0) {
            AtomicInteger counter = userConnectionCounter.get(userId, key -> new AtomicInteger());
            if (counter != null) {
                counter.incrementAndGet();
            }
        }
    }

    private void releaseConnection(Long userId) {
        if (userId == null) {
            return;
        }
        totalConnections.decrementAndGet();
        AtomicInteger counter = userConnectionCounter == null ? null : userConnectionCounter.getIfPresent(userId);
        if (counter == null) {
            return;
        }
        if (counter.decrementAndGet() <= 0) {
            userConnectionCounter.invalidate(userId);
        }
    }

    private SseEmitter rejectConnection(Long userId) {
        long retryAfterMillis = resolveRetryAfterMillis();
        SseEmitter emitter = new SseEmitter(2000L);
        NoticePushPayload payload = new NoticePushPayload(
                null,
                null,
                null,
                null,
                null,
                null,
                noticeConstants.getStream().getHeartbeatIntervalMillis(),
                noticeConstants.getStream().getHeartbeatTimeoutMillis(),
                retryAfterMillis,
                "rejected"
        );
        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name(noticeConstants.getStream().getEventInitName())
                    .data(payload);
            if (retryAfterMillis > 0) {
                builder.reconnectTime(retryAfterMillis);
            }
            emitter.send(builder);
        } catch (IOException ex) {
            if (log.isDebugEnabled()) {
                log.debug(noticeConstants.getStream().getLogInitFailed(), userId, ex);
            }
        } finally {
            emitter.complete();
        }
        return emitter;
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
