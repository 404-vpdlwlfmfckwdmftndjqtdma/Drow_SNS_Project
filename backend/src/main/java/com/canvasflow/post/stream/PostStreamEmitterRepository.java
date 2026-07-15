package com.canvasflow.post.stream;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * 단일 SSE 연결당 구독 중인 postId 집합을 저장한다.
 * 게시글 수가 늘어도 브라우저 연결 수는 1개로 유지하고 서버에서 postId로 팬아웃한다.
 */
@Repository
public class PostStreamEmitterRepository {

    private final Map<SseEmitter, Set<Long>> subscriptionsByEmitter = new ConcurrentHashMap<>();

    public void save(SseEmitter emitter, Set<Long> postIds) {
        subscriptionsByEmitter.put(emitter, new CopyOnWriteArraySet<>(postIds));
    }

    public void remove(SseEmitter emitter) {
        subscriptionsByEmitter.remove(emitter);
    }

    public Set<SseEmitter> findByPostId(Long postId) {
        return subscriptionsByEmitter.entrySet().stream()
                .filter(entry -> entry.getValue().contains(postId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<SseEmitter> findAll() {
        return subscriptionsByEmitter.keySet();
    }
}
