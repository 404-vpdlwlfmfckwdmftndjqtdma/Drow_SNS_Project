package com.canvasflow.notification.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 유저별 연결된 SseEmitter를 메모리에 들고 있는 레지스트리.
 * 인스턴스가 여러 대로 늘어나면(수평 확장) 이 방식으로는 다른 인스턴스에 붙은 유저에게 못 보내므로,
 * 그때는 Redis pub/sub 등으로 브로드캐스트를 바꿔야 한다 (지금은 단일 인스턴스 가정).
 */
@Repository
public class SseEmitterRepository {

    private final Map<Long, List<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public void save(Long userId, SseEmitter emitter) {
        emittersByUserId.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(Long userId, SseEmitter emitter) {
        emittersByUserId.computeIfPresent(userId, (id, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    public List<SseEmitter> findByUserId(Long userId) {
        return emittersByUserId.getOrDefault(userId, List.of());
    }

    public List<SseEmitter> findAll() {
        return emittersByUserId.values().stream().flatMap(List::stream).toList();
    }
}
