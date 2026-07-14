package com.canvasflow.comment.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 게시글(postId) 하나의 댓글 목록을 보고 있는 모든 클라이언트의 SseEmitter를 들고 있는 레지스트리.
 * like의 LikeEmitterRepository와 같은 구조 - 유저가 아니라 postId 단위로 브로드캐스트한다.
 * 단일 인스턴스 가정 - 여러 대로 늘리면 Redis pub/sub 등으로 교체 필요.
 */
@Repository
public class CommentEmitterRepository {

    private final Map<Long, List<SseEmitter>> emittersByPostId = new ConcurrentHashMap<>();

    public void save(Long postId, SseEmitter emitter) {
        emittersByPostId.computeIfAbsent(postId, id -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(Long postId, SseEmitter emitter) {
        emittersByPostId.computeIfPresent(postId, (id, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    public List<SseEmitter> findByPostId(Long postId) {
        return emittersByPostId.getOrDefault(postId, List.of());
    }

    public List<SseEmitter> findAll() {
        return emittersByPostId.values().stream().flatMap(List::stream).toList();
    }
}
