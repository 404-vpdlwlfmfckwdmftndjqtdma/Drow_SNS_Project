package com.canvasflow.post.stream;

import com.canvasflow.comment.dto.CommentDeletedEvent;
import com.canvasflow.comment.dto.CommentLikeCountEvent;
import com.canvasflow.comment.dto.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;

/**
 * 피드에서 좋아요/댓글 관련 이벤트를 단일 SSE 채널로 멀티플렉싱한다.
 * 게시글 수가 많아져도 브라우저 연결 수는 창당 1개로 유지하는 것이 목적이다.
 */
@RequiredArgsConstructor
@Service
public class PostStreamService {

    private static final long HEARTBEAT_INTERVAL_MS = 30_000;
    private static final long SUBSCRIBE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final PostStreamEmitterRepository postStreamEmitterRepository;

    // 한 연결에서 여러 postId를 구독하므로, emitter마다 구독 postId 집합을 저장한다.
    public SseEmitter subscribe(Set<Long> postIds) {
        SseEmitter emitter = new SseEmitter(SUBSCRIBE_TIMEOUT_MS);
        postStreamEmitterRepository.save(emitter, postIds);

        emitter.onCompletion(() -> postStreamEmitterRepository.remove(emitter));
        emitter.onTimeout(() -> postStreamEmitterRepository.remove(emitter));
        emitter.onError(e -> postStreamEmitterRepository.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            postStreamEmitterRepository.remove(emitter);
        }
        return emitter;
    }

    // postId를 키로 해당 게시글을 구독 중인 emitter에만 fan-out 한다.
    public void publishPostLikeCount(Long postId, long likeCount) {
        PostStreamEvent payload = PostStreamEvent.postLikeCount(postId, likeCount);
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("post-like-count").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    // 피드 카드의 댓글 수 배지를 갱신하는 이벤트.
    public void publishPostCommentCount(Long postId, long commentCount) {
        PostStreamEvent payload = PostStreamEvent.postCommentCount(postId, commentCount);
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("post-comment-count").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    // 댓글 모달에서 목록 상태를 바로 반영할 수 있도록 댓글 본문 이벤트도 같은 채널로 보낸다.
    public void publishCommentCreated(CommentResponse comment) {
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(comment.postId())) {
            try {
                emitter.send(SseEmitter.event().name("comment-created").data(comment));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    // 수정 이벤트도 postId 구독자에게만 전달.
    public void publishCommentUpdated(CommentResponse comment) {
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(comment.postId())) {
            try {
                emitter.send(SseEmitter.event().name("comment-updated").data(comment));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    // 삭제/좋아요카운트 payload에는 postId를 포함해 클라이언트가 안전하게 라우팅하도록 한다.
    public void publishCommentDeleted(Long postId, CommentDeletedEvent event) {
        PostScopedCommentDeletedEvent payload = new PostScopedCommentDeletedEvent(postId, event.id(), event.hardDeleted());
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("comment-deleted").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    // 댓글 좋아요 수 변경 이벤트.
    public void publishCommentLikeCount(Long postId, CommentLikeCountEvent event) {
        PostScopedCommentLikeCountEvent payload = new PostScopedCommentLikeCountEvent(postId, event.commentId(), event.likeCount());
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("comment-like-count").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    // 중간 프록시 유휴 타임아웃 방지를 위한 heartbeat.
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        for (SseEmitter emitter : postStreamEmitterRepository.findAll()) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                emitter.complete();
            }
        }
    }

    public record PostScopedCommentDeletedEvent(
            Long postId,
            Long id,
            boolean hardDeleted
    ) {
    }

    public record PostScopedCommentLikeCountEvent(
            Long postId,
            Long commentId,
            Long likeCount
    ) {
    }
}
