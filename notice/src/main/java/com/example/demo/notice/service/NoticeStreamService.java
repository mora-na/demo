package com.example.demo.notice.service;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.cluster.NodeIdProvider;
import com.example.demo.notice.config.NoticeConstants;
import com.example.demo.notice.dto.*;
import com.example.demo.notice.entity.Notice;
import com.example.demo.notice.mapper.NoticeStreamNodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    private static final String CACHE_PREFIX = "notice:stream:";
    private static final String LATEST_CACHE_KEY_PREFIX = CACHE_PREFIX + "latest:";
    private static final String CONNECTION_COUNTER_KEY_PREFIX = CACHE_PREFIX + "conn:";
    private static final String TOTAL_CONNECTION_COUNTER_KEY = CACHE_PREFIX + "conn:total";
    private static final String NODE_CONNECTION_COUNTER_KEY_PREFIX = CACHE_PREFIX + "conn:node:";
    private static final String DISPATCH_SEQ_KEY = CACHE_PREFIX + "seq";
    private static final String DISPATCH_EVENT_KEY_PREFIX = CACHE_PREFIX + "event:";

    private final NoticeConstants noticeConstants;
    private final CacheTool cacheTool;
    private final NoticeStreamNodeMapper streamNodeMapper;
    private final NodeIdProvider nodeIdProvider;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, Long> emitterLastActive = new ConcurrentHashMap<>();
    private final AtomicInteger totalConnections = new AtomicInteger();
    private final AtomicLong lastDispatchSeq = new AtomicLong(-1L);
    private final AtomicLong lastNodeReportAt = new AtomicLong(0L);
    private final AtomicLong cachedGlobalTotal = new AtomicLong(0L);
    private final AtomicLong lastGlobalRefreshAt = new AtomicLong(0L);
    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledExecutorService dispatchExecutor;

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
        touchEmitter(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError((ex) -> removeEmitter(userId, emitter));
        if (latestNotices != null && isLatestCacheEnabled()) {
            writeLatestCache(userId, buildLatestList(latestNotices));
        }
        refreshConnectionCounter(userId);
        refreshTotalConnectionCounter();
        reportNodeHeartbeat();
        sendInitEvent(userId, emitter, unreadCount);
        return emitter;
    }

    public void pushToUsers(Collection<Long> userIds, Notice notice, Map<Long, Long> unreadCounts) {
        if (userIds == null || userIds.isEmpty() || notice == null) {
            return;
        }
        NoticeStreamDispatchEvent event = buildNoticeEvent(userIds, notice, unreadCounts);
        if (event == null) {
            return;
        }
        long seq = publishEvent(event);
        lastDispatchSeq.updateAndGet(prev -> Math.max(prev, seq));
        dispatchEvent(event);
    }

    public void pushUnreadCounts(Collection<Long> userIds, Map<Long, Long> unreadCounts, Collection<Long> removedNoticeIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        NoticeStreamDispatchEvent event = buildUnreadEvent(userIds, unreadCounts, removedNoticeIds);
        if (event == null) {
            return;
        }
        long seq = publishEvent(event);
        lastDispatchSeq.updateAndGet(prev -> Math.max(prev, seq));
        dispatchEvent(event);
    }

    private NoticeStreamDispatchEvent buildNoticeEvent(Collection<Long> userIds, Notice notice, Map<Long, Long> unreadCounts) {
        if (notice == null || userIds == null || userIds.isEmpty()) {
            return null;
        }
        NoticeStreamDispatchNotice summary = new NoticeStreamDispatchNotice(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedName(),
                notice.getCreateTime()
        );
        List<Long> targets = new ArrayList<>();
        for (Long userId : userIds) {
            if (userId != null) {
                targets.add(userId);
            }
        }
        if (targets.isEmpty()) {
            return null;
        }
        Map<Long, Long> counts = unreadCounts == null ? null : new LinkedHashMap<>(unreadCounts);
        return new NoticeStreamDispatchEvent(
                NoticeStreamDispatchType.NOTICE,
                targets,
                counts,
                null,
                summary
        );
    }

    private NoticeStreamDispatchEvent buildUnreadEvent(Collection<Long> userIds,
                                                       Map<Long, Long> unreadCounts,
                                                       Collection<Long> removedNoticeIds) {
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        List<Long> targets = new ArrayList<>();
        for (Long userId : userIds) {
            if (userId != null) {
                targets.add(userId);
            }
        }
        if (targets.isEmpty()) {
            return null;
        }
        List<Long> removed = null;
        if (removedNoticeIds != null && !removedNoticeIds.isEmpty()) {
            removed = new ArrayList<>();
            for (Long id : removedNoticeIds) {
                if (id != null) {
                    removed.add(id);
                }
            }
        }
        Map<Long, Long> counts = unreadCounts == null ? null : new LinkedHashMap<>(unreadCounts);
        return new NoticeStreamDispatchEvent(
                NoticeStreamDispatchType.UNREAD_UPDATE,
                targets,
                counts,
                removed,
                null
        );
    }

    private long publishEvent(NoticeStreamDispatchEvent event) {
        if (event == null) {
            return -1L;
        }
        long seq = nextDispatchSeq();
        cacheTool.set(buildDispatchEventKey(seq), event, dispatchEventTtl());
        return seq;
    }

    public NoticeStreamMetricsVO snapshotMetrics() {
        NoticeStreamMetricsVO metrics = new NoticeStreamMetricsVO();
        metrics.setTotalConnections(Math.max(0L, resolveTotalConnections()));
        metrics.setActiveUsers(emitters.size());
        metrics.setLatestCacheSize(emitters.size());
        metrics.setConnectionCounterSize(emitters.size());
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
        cleanupLocalNodeCounters();
        long interval = noticeConstants.getStream().getHeartbeatIntervalMillis();
        long cleanupInterval = resolveEmitterCleanupIntervalMillis();
        if (interval <= 0 && cleanupInterval <= 0) {
            log.info(noticeConstants.getStream().getLogHeartbeatDisabled(), interval);
            return;
        }
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, noticeConstants.getStream().getHeartbeatThreadName());
            thread.setDaemon(true);
            return thread;
        });
        if (interval > 0) {
            heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, interval, interval, TimeUnit.MILLISECONDS);
        } else {
            log.info(noticeConstants.getStream().getLogHeartbeatDisabled(), interval);
        }
        if (cleanupInterval > 0) {
            heartbeatExecutor.scheduleAtFixedRate(this::cleanupIdleEmitters, cleanupInterval, cleanupInterval, TimeUnit.MILLISECONDS);
        }
    }

    @PostConstruct
    public void startDispatch() {
        long interval = noticeConstants.getStream().getDispatchPollIntervalMillis();
        if (interval <= 0) {
            log.info(noticeConstants.getStream().getLogDispatchDisabled(), interval);
            return;
        }
        dispatchExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, noticeConstants.getStream().getDispatchThreadName());
            thread.setDaemon(true);
            return thread;
        });
        dispatchExecutor.scheduleWithFixedDelay(this::pollDispatchEvents, interval, interval, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stopHeartbeat() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
        if (dispatchExecutor != null) {
            dispatchExecutor.shutdownNow();
        }
        cleanupLocalNodeCounters();
    }

    private void pollDispatchEvents() {
        Long current = parseLong(cacheTool.get(DISPATCH_SEQ_KEY));
        if (current == null || current <= 0L) {
            return;
        }
        long last = lastDispatchSeq.get();
        if (last < 0L) {
            lastDispatchSeq.compareAndSet(last, current);
            return;
        }
        if (current < last) {
            lastDispatchSeq.set(current);
            return;
        }
        if (emitters.isEmpty()) {
            lastDispatchSeq.set(current);
            return;
        }
        long maxBatch = resolveDispatchMaxBatchSize();
        long start = last + 1L;
        if (current - last > maxBatch) {
            start = Math.max(start, current - maxBatch + 1L);
        }
        List<String> keys = new ArrayList<>();
        for (long seq = start; seq <= current; seq++) {
            keys.add(buildDispatchEventKey(seq));
        }
        List<Object> values = cacheTool.multiGet(keys);
        if (values != null && !values.isEmpty()) {
            for (Object value : values) {
                if (value instanceof NoticeStreamDispatchEvent) {
                    dispatchEvent((NoticeStreamDispatchEvent) value);
                }
            }
        }
        lastDispatchSeq.set(current);
    }

    private void dispatchEvent(NoticeStreamDispatchEvent event) {
        if (event == null || event.getType() == null) {
            return;
        }
        if (event.getType() == NoticeStreamDispatchType.NOTICE) {
            dispatchNoticeEvent(event);
            return;
        }
        if (event.getType() == NoticeStreamDispatchType.UNREAD_UPDATE) {
            dispatchUnreadEvent(event);
        }
    }

    private void dispatchNoticeEvent(NoticeStreamDispatchEvent event) {
        NoticeStreamDispatchNotice notice = event.getNotice();
        if (notice == null) {
            return;
        }
        List<Long> targets = normalizeIds(event.getUserIds());
        if (targets.isEmpty()) {
            return;
        }
        for (Long userId : targets) {
            if (userId == null) {
                continue;
            }
            List<SseEmitter> userEmitters = emitters.get(userId);
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            List<NoticeLatestVO> latestSnapshot = updateLatestCache(userId, notice);
            Long unreadCount = resolveUnreadCount(event.getUnreadCounts(), userId);
            NoticePushPayload payload = new NoticePushPayload(
                    notice.getId(),
                    notice.getTitle(),
                    notice.getCreatedName(),
                    notice.getCreatedAt(),
                    unreadCount == null ? noticeConstants.getNumeric().getZeroLong() : unreadCount,
                    latestSnapshot,
                    null,
                    null,
                    null,
                    null
            );
            refreshConnectionCounter(userId);
            refreshTotalConnectionCounter();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventNoticeName()).data(payload));
                    touchEmitter(emitter);
                } catch (IOException ex) {
                    log.debug(noticeConstants.getStream().getLogPushFailed(), userId, ex);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    private void dispatchUnreadEvent(NoticeStreamDispatchEvent event) {
        List<Long> targets = normalizeIds(event.getUserIds());
        if (targets.isEmpty()) {
            return;
        }
        List<Long> removed = normalizeIds(event.getRemovedNoticeIds());
        boolean hasRemoved = !removed.isEmpty();
        Set<Long> removedSet = hasRemoved ? new HashSet<>(removed) : Collections.emptySet();
        for (Long userId : targets) {
            if (userId == null) {
                continue;
            }
            List<SseEmitter> userEmitters = emitters.get(userId);
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            List<NoticeLatestVO> latestSnapshot = hasRemoved
                    ? removeFromLatestCache(userId, removedSet)
                    : latestSnapshot(userId);
            Long unreadCount = resolveUnreadCount(event.getUnreadCounts(), userId);
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
            refreshConnectionCounter(userId);
            refreshTotalConnectionCounter();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventNoticeName()).data(payload));
                    touchEmitter(emitter);
                } catch (IOException ex) {
                    log.debug(noticeConstants.getStream().getLogPushUpdateFailed(), userId, ex);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        if (userId == null || emitter == null) {
            return;
        }
        emitterLastActive.remove(emitter);
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
            if (isLatestCacheEnabled()) {
                cacheTool.delete(buildLatestCacheKey(userId));
            }
        }
        reportNodeHeartbeat();
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
            refreshConnectionCounter(userId);
            refreshTotalConnectionCounter();
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name(noticeConstants.getStream().getEventInitName())
                    .data(payload);
            long retryAfterMillis = resolveRetryAfterMillis();
            if (retryAfterMillis > 0) {
                builder.reconnectTime(retryAfterMillis);
            }
            emitter.send(builder);
            touchEmitter(emitter);
        } catch (IOException ex) {
            log.debug(noticeConstants.getStream().getLogInitFailed(), userId, ex);
            removeEmitter(userId, emitter);
        }
    }

    private List<NoticeLatestVO> buildLatestList(List<NoticeLatestVO> latestNotices) {
        int limit = latestLimit();
        if (limit <= 0) {
            return new ArrayList<>();
        }
        List<NoticeLatestVO> list = new ArrayList<>();
        if (latestNotices != null) {
            for (NoticeLatestVO item : latestNotices) {
                if (item == null) {
                    continue;
                }
                list.add(item);
                if (list.size() >= limit) {
                    break;
                }
            }
        }
        return list;
    }

    private List<NoticeLatestVO> updateLatestCache(Long userId, Notice notice) {
        if (userId == null || notice == null) {
            return latestSnapshot(userId);
        }
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        return updateLatestCache(userId,
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedName(),
                notice.getCreateTime());
    }

    private List<NoticeLatestVO> updateLatestCache(Long userId, NoticeStreamDispatchNotice notice) {
        if (userId == null || notice == null) {
            return latestSnapshot(userId);
        }
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        return updateLatestCache(userId,
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedName(),
                notice.getCreatedAt());
    }

    private List<NoticeLatestVO> updateLatestCache(Long userId,
                                                   Long noticeId,
                                                   String title,
                                                   String createdName,
                                                   LocalDateTime createdAt) {
        int limit = latestLimit();
        List<NoticeLatestVO> current = readLatestCache(userId);
        List<NoticeLatestVO> updated = new ArrayList<>(current);
        if (noticeId != null) {
            updated.removeIf(item -> Objects.equals(item.getId(), noticeId));
        }
        NoticeLatestVO latest = new NoticeLatestVO(
                noticeId,
                title,
                createdName,
                createdAt,
                noticeConstants.getRecipient().getUnread(),
                null
        );
        updated.add(0, latest);
        if (updated.size() > limit) {
            updated = new ArrayList<>(updated.subList(0, limit));
        }
        writeLatestCache(userId, updated);
        return updated;
    }

    private List<NoticeLatestVO> latestSnapshot(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        List<NoticeLatestVO> snapshot = readLatestCache(userId);
        if (snapshot.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(snapshot);
    }

    private List<NoticeLatestVO> removeFromLatestCache(Long userId, Set<Long> removedNoticeIds) {
        if (userId == null || removedNoticeIds == null || removedNoticeIds.isEmpty()) {
            return latestSnapshot(userId);
        }
        if (!isLatestCacheEnabled()) {
            return new ArrayList<>();
        }
        List<NoticeLatestVO> current = readLatestCache(userId);
        if (current.isEmpty()) {
            return new ArrayList<>();
        }
        List<NoticeLatestVO> updated = new ArrayList<>(current);
        updated.removeIf(item -> item != null && removedNoticeIds.contains(item.getId()));
        writeLatestCache(userId, updated);
        return updated;
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

    private void touchEmitter(SseEmitter emitter) {
        if (emitter == null) {
            return;
        }
        emitterLastActive.put(emitter, System.currentTimeMillis());
    }

    private long resolveEmitterCleanupTimeoutMillis() {
        long configured = noticeConstants.getStream().getEmitterCleanupTimeoutMillis();
        if (configured > 0) {
            return configured;
        }
        long heartbeatTimeout = noticeConstants.getStream().getHeartbeatTimeoutMillis();
        if (heartbeatTimeout > 0) {
            return heartbeatTimeout;
        }
        return resolveEmitterTimeoutMillis();
    }

    private long resolveEmitterCleanupIntervalMillis() {
        long configured = noticeConstants.getStream().getEmitterCleanupIntervalMillis();
        if (configured > 0) {
            return configured;
        }
        long timeout = resolveEmitterCleanupTimeoutMillis();
        if (timeout <= 0) {
            return 0L;
        }
        long interval = timeout / 2;
        interval = Math.max(1000L, interval);
        return Math.min(interval, 60000L);
    }

    private void cleanupIdleEmitters() {
        long timeout = resolveEmitterCleanupTimeoutMillis();
        if (timeout <= 0 || emitters.isEmpty()) {
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
                Long last = emitterLastActive.get(emitter);
                if (last == null) {
                    touchEmitter(emitter);
                    continue;
                }
                if (now - last > timeout) {
                    removeEmitter(userId, emitter);
                    try {
                        emitter.complete();
                    } catch (RuntimeException ignored) {
                        // ignore
                    }
                }
            }
        }
    }

    private long resolveRetryAfterMillis() {
        long retryAfterMillis = noticeConstants.getStream().getRetryAfterMillis();
        if (retryAfterMillis <= 0) {
            return 0L;
        }
        return retryAfterMillis;
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
            long total = resolveTotalConnections();
            if (ratio > 0 && total >= Math.ceil(maxTotal * ratio)) {
                return true;
            }
        }
        long maxCacheSize = stream.getLatestCacheMaxSize();
        if (maxCacheSize > 0) {
            double ratio = resolveDegradeCacheRatio();
            if (ratio > 0 && emitters.size() >= Math.ceil(maxCacheSize * ratio)) {
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
        long globalTotal = incrementTotalConnectionCounter();
        if (maxTotal > 0 && globalTotal > maxTotal) {
            decrementTotalConnectionCounter();
            return false;
        }
        int maxPerUser = noticeConstants.getStream().getMaxConnectionsPerUser();
        if (maxPerUser > 0) {
            long counter = incrementConnectionCounter(userId);
            if (counter > maxPerUser) {
                decrementConnectionCounter(userId);
                decrementTotalConnectionCounter();
                return false;
            }
        }
        totalConnections.incrementAndGet();
        return true;
    }

    private void acquireConnection(Long userId) {
        if (userId == null) {
            return;
        }
        incrementTotalConnectionCounter();
        totalConnections.incrementAndGet();
        int maxPerUser = noticeConstants.getStream().getMaxConnectionsPerUser();
        if (maxPerUser > 0) {
            incrementConnectionCounter(userId);
        }
    }

    private void releaseConnection(Long userId) {
        if (userId == null) {
            return;
        }
        totalConnections.decrementAndGet();
        decrementTotalConnectionCounter();
        decrementConnectionCounter(userId);
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
        reportNodeHeartbeat();
        refreshTotalConnectionCounter();
        long now = System.currentTimeMillis();
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            Long userId = entry.getKey();
            List<SseEmitter> userEmitters = entry.getValue();
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            refreshConnectionCounter(userId);
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(noticeConstants.getStream().getEventPingName()).data(now));
                    touchEmitter(emitter);
                } catch (IOException ex) {
                    if (log.isDebugEnabled()) {
                        log.debug(noticeConstants.getStream().getLogHeartbeatFailed(), userId, ex.getMessage());
                    }
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    private String buildLatestCacheKey(Long userId) {
        return LATEST_CACHE_KEY_PREFIX + userId;
    }

    private String buildConnectionCounterKey(Long userId) {
        return CONNECTION_COUNTER_KEY_PREFIX + userId;
    }

    private String buildTotalConnectionKey() {
        return TOTAL_CONNECTION_COUNTER_KEY;
    }

    private String buildNodeConnectionKey(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return null;
        }
        return NODE_CONNECTION_COUNTER_KEY_PREFIX + nodeId;
    }

    private String buildDispatchEventKey(long seq) {
        return DISPATCH_EVENT_KEY_PREFIX + seq;
    }

    private Duration latestCacheTtl() {
        int minutes = noticeConstants.getStream().getLatestCacheExpireMinutes();
        return Duration.ofMinutes(Math.max(1, minutes));
    }

    private Duration connectionCounterTtl() {
        int minutes = noticeConstants.getStream().getConnectionCounterExpireMinutes();
        return Duration.ofMinutes(Math.max(1, minutes));
    }

    private Duration dispatchEventTtl() {
        int seconds = noticeConstants.getStream().getDispatchEventTtlSeconds();
        if (seconds <= 0) {
            seconds = 300;
        }
        return Duration.ofSeconds(seconds);
    }

    private long resolveDispatchMaxBatchSize() {
        int size = noticeConstants.getStream().getDispatchMaxBatchSize();
        return Math.max(1, size);
    }

    private long nextDispatchSeq() {
        Long next = cacheTool.increment(DISPATCH_SEQ_KEY);
        if (next == null) {
            Long current = parseLong(cacheTool.get(DISPATCH_SEQ_KEY));
            next = current == null ? 1L : current + 1L;
            cacheTool.set(DISPATCH_SEQ_KEY, next, dispatchEventTtl());
            return next;
        }
        cacheTool.expire(DISPATCH_SEQ_KEY, dispatchEventTtl());
        return next;
    }

    private List<NoticeLatestVO> readLatestCache(Long userId) {
        Object value = cacheTool.get(buildLatestCacheKey(userId));
        List<NoticeLatestVO> result = extractLatestList(value);
        if (result.isEmpty()) {
            return new ArrayList<>();
        }
        cacheTool.expire(buildLatestCacheKey(userId), latestCacheTtl());
        return result;
    }

    private void writeLatestCache(Long userId, List<NoticeLatestVO> value) {
        if (userId == null) {
            return;
        }
        NoticeLatestCacheEntry entry = new NoticeLatestCacheEntry(value == null ? new ArrayList<>() : value);
        cacheTool.set(buildLatestCacheKey(userId), entry, latestCacheTtl());
    }

    private long incrementConnectionCounter(Long userId) {
        if (userId == null) {
            return 0L;
        }
        String key = buildConnectionCounterKey(userId);
        Long next = cacheTool.incr(key);
        if (next == null) {
            return 0L;
        }
        cacheTool.expire(key, connectionCounterTtl());
        return Math.max(0L, next);
    }

    private long decrementConnectionCounter(Long userId) {
        if (userId == null) {
            return 0L;
        }
        String key = buildConnectionCounterKey(userId);
        Long next = cacheTool.decr(key);
        if (next == null || next <= 0L) {
            cacheTool.delete(key);
            return 0L;
        }
        cacheTool.expire(key, connectionCounterTtl());
        return next;
    }

    private long incrementTotalConnectionCounter() {
        String totalKey = buildTotalConnectionKey();
        Long total = cacheTool.incr(totalKey);
        if (total == null) {
            return 0L;
        }
        cacheTool.expire(totalKey, connectionCounterTtl());
        String nodeKey = buildNodeConnectionKey(resolveNodeId());
        if (nodeKey != null) {
            Long next = cacheTool.incr(nodeKey);
            if (next != null) {
                cacheTool.expire(nodeKey, connectionCounterTtl());
            }
        }
        return Math.max(0L, total);
    }

    private long decrementTotalConnectionCounter() {
        String totalKey = buildTotalConnectionKey();
        Long total = cacheTool.decr(totalKey);
        if (total == null || total <= 0L) {
            cacheTool.delete(totalKey);
            total = 0L;
        } else {
            cacheTool.expire(totalKey, connectionCounterTtl());
        }
        String nodeKey = buildNodeConnectionKey(resolveNodeId());
        if (nodeKey != null) {
            Long next = cacheTool.decr(nodeKey);
            if (next == null || next <= 0L) {
                cacheTool.delete(nodeKey);
            } else {
                cacheTool.expire(nodeKey, connectionCounterTtl());
            }
        }
        return total;
    }

    private void refreshConnectionCounter(Long userId) {
        if (userId == null) {
            return;
        }
        cacheTool.expire(buildConnectionCounterKey(userId), connectionCounterTtl());
    }

    private void refreshTotalConnectionCounter() {
        cacheTool.expire(buildTotalConnectionKey(), connectionCounterTtl());
        String nodeKey = buildNodeConnectionKey(resolveNodeId());
        if (nodeKey != null) {
            cacheTool.expire(nodeKey, connectionCounterTtl());
        }
    }

    private long resolveTotalConnections() {
        long now = System.currentTimeMillis();
        long lastAt = lastGlobalRefreshAt.get();
        if (now - lastAt < 1000L) {
            return Math.max(0L, cachedGlobalTotal.get());
        }
        long total = resolveTotalFromNodes();
        cachedGlobalTotal.set(Math.max(0L, total));
        lastGlobalRefreshAt.set(now);
        return Math.max(0L, total);
    }

    private long resolveTotalFromNodes() {
        if (streamNodeMapper == null) {
            Long value = parseLong(cacheTool.get(buildTotalConnectionKey()));
            return value == null ? Math.max(0, totalConnections.get()) : value;
        }
        LocalDateTime minHeartbeat = LocalDateTime.now().minusSeconds(resolveNodeHeartbeatExpireSeconds());
        Long total = streamNodeMapper.sumActiveConnections(minHeartbeat);
        return total == null ? 0L : total;
    }

    private String resolveNodeId() {
        return nodeIdProvider == null ? null : nodeIdProvider.get();
    }

    private void cleanupLocalNodeCounters() {
        String nodeId = resolveNodeId();
        if (nodeId == null) {
            return;
        }
        String nodeKey = buildNodeConnectionKey(nodeId);
        Long local = parseLong(cacheTool.get(nodeKey));
        if (local != null && local > 0) {
            Long total = cacheTool.incrBy(buildTotalConnectionKey(), -local);
            if (total == null || total <= 0L) {
                cacheTool.delete(buildTotalConnectionKey());
            }
        }
        cacheTool.delete(nodeKey);
        if (streamNodeMapper != null) {
            try {
                streamNodeMapper.deleteById(nodeId);
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private int resolveNodeHeartbeatExpireSeconds() {
        int minutes = noticeConstants.getStream().getConnectionCounterExpireMinutes();
        int fromCounter = minutes > 0 ? minutes * 60 : 60;
        long timeoutMs = noticeConstants.getStream().getHeartbeatTimeoutMillis();
        int fromHeartbeat = timeoutMs > 0 ? (int) Math.ceil(timeoutMs / 1000.0) * 2 : 60;
        return Math.max(fromCounter, fromHeartbeat);
    }

    private void reportNodeHeartbeat() {
        if (streamNodeMapper == null || nodeIdProvider == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = lastNodeReportAt.get();
        if (now - last < 500L) {
            return;
        }
        lastNodeReportAt.set(now);
        String nodeId = nodeIdProvider.get();
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return;
        }
        long count = Math.max(0, totalConnections.get());
        LocalDateTime heartbeatAt = LocalDateTime.now();
        int updated = streamNodeMapper.updateHeartbeat(nodeId, count, heartbeatAt);
        if (updated == 0) {
            streamNodeMapper.insertHeartbeat(nodeId, count, heartbeatAt);
        }
    }

    private Long parseLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<NoticeLatestVO> extractLatestList(Object value) {
        if (value instanceof NoticeLatestCacheEntry) {
            NoticeLatestCacheEntry entry = (NoticeLatestCacheEntry) value;
            List<NoticeLatestVO> latest = entry.getLatest();
            return latest == null ? new ArrayList<>() : new ArrayList<>(latest);
        }
        if (value instanceof List) {
            List<?> raw = (List<?>) value;
            if (raw.isEmpty()) {
                return new ArrayList<>();
            }
            List<NoticeLatestVO> result = new ArrayList<>(raw.size());
            for (Object item : raw) {
                if (item instanceof NoticeLatestVO) {
                    result.add((NoticeLatestVO) item);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private List<Long> normalizeIds(Collection<?> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>(raw.size());
        for (Object item : raw) {
            Long parsed = parseLong(item);
            if (parsed != null) {
                result.add(parsed);
            }
        }
        return result;
    }

    private Long resolveUnreadCount(Map<Long, Long> unreadCounts, Long userId) {
        if (userId == null) {
            return noticeConstants.getNumeric().getZeroLong();
        }
        if (unreadCounts == null || unreadCounts.isEmpty()) {
            return noticeConstants.getNumeric().getZeroLong();
        }
        Long direct = unreadCounts.get(userId);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<?, ?> entry : unreadCounts.entrySet()) {
            Long key = parseLong(entry.getKey());
            if (key != null && key.equals(userId)) {
                return parseLong(entry.getValue());
            }
        }
        return noticeConstants.getNumeric().getZeroLong();
    }
}
