package com.canvasflow.like.sse;

import com.canvasflow.like.entity.LikeTargetType;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * (targetType, targetId) 하나를 보고 있는 모든 클라이언트의 SseEmitter를 들고 있는 레지스트리.
 * notification의 SseEmitterRepository와 같은 구조지만, 키가 유저 단위가 아니라 "대상" 단위라는 점이 다르다
 * (좋아요 개수는 특정 유저가 아니라 그 게시글/댓글을 보고 있는 모두에게 브로드캐스트해야 하므로).
 * 단일 인스턴스 가정 - 여러 대로 늘리면 Redis pub/sub 등으로 교체 필요.
 */
@Repository
public class LikeEmitterRepository {

    private final Map<String, List<SseEmitter>> emittersByTarget = new ConcurrentHashMap<>();

    private String key(LikeTargetType targetType, Long targetId) {
        return targetType.name() + ":" + targetId;
    }

    public void save(LikeTargetType targetType, Long targetId, SseEmitter emitter) {
        emittersByTarget.computeIfAbsent(key(targetType, targetId), k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(LikeTargetType targetType, Long targetId, SseEmitter emitter) {
        emittersByTarget.computeIfPresent(key(targetType, targetId), (k, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    public List<SseEmitter> findByTarget(LikeTargetType targetType, Long targetId) {
        return emittersByTarget.getOrDefault(key(targetType, targetId), List.of());
    }

    public List<SseEmitter> findAll() {
        return emittersByTarget.values().stream().flatMap(List::stream).toList();
    }
}
