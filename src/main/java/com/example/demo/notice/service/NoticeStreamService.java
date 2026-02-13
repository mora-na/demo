package com.example.demo.notice.service;

import com.example.demo.notice.dto.NoticePushPayload;
import com.example.demo.notice.entity.Notice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通知 SSE 流管理，负责建立连接与推送新通知事件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Slf4j
@Service
public class NoticeStreamService {

    private static final long TIMEOUT_MILLIS = 0L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((ex) -> removeEmitter(userId, emitter));
        return emitter;
    }

    public void pushToUsers(Collection<Long> userIds, Notice notice) {
        if (userIds == null || userIds.isEmpty() || notice == null) {
            return;
        }
        NoticePushPayload payload = new NoticePushPayload(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedName(),
                notice.getCreateTime()
        );
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            List<SseEmitter> userEmitters = emitters.get(userId);
            if (userEmitters == null || userEmitters.isEmpty()) {
                continue;
            }
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("notice").data(payload));
                } catch (IOException ex) {
                    log.debug("Failed to push notice to user {}, removing emitter.", userId, ex);
                    removeEmitter(userId, emitter);
                }
            }
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
        }
    }
}
