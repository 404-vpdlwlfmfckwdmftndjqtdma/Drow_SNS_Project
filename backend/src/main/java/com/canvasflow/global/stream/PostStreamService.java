package com.canvasflow.global.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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

    public void publishCommentCreated(
            Long postId,
            Long id,
            Long parentId,
            Long writerId,
            String writerNickname,
            String writerProfileImageUrl,
            String content,
            boolean deleted,
            LocalDateTime createdAt,
            long likeCount,
            boolean likedByMe
    ) {
        PostCommentEvent payload = new PostCommentEvent(
                id,
                postId,
                parentId,
                writerId,
                writerNickname,
                writerProfileImageUrl,
                content,
                deleted,
                createdAt,
                likeCount,
                likedByMe,
                List.of()
        );

        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("comment-created").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    public void publishCommentUpdated(
            Long postId,
            Long id,
            Long parentId,
            Long writerId,
            String writerNickname,
            String writerProfileImageUrl,
            String content,
            boolean deleted,
            LocalDateTime createdAt,
            long likeCount,
            boolean likedByMe
    ) {
        PostCommentEvent payload = new PostCommentEvent(
                id,
                postId,
                parentId,
                writerId,
                writerNickname,
                writerProfileImageUrl,
                content,
                deleted,
                createdAt,
                likeCount,
                likedByMe,
                List.of()
        );

        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("comment-updated").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    public void publishCommentDeleted(Long postId, Long id, boolean hardDeleted) {
        PostScopedCommentDeletedEvent payload = new PostScopedCommentDeletedEvent(postId, id, hardDeleted);
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("comment-deleted").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

    public void publishCommentLikeCount(Long postId, Long commentId, long likeCount) {
        PostScopedCommentLikeCountEvent payload = new PostScopedCommentLikeCountEvent(postId, commentId, likeCount);
        for (SseEmitter emitter : postStreamEmitterRepository.findByPostId(postId)) {
            try {
                emitter.send(SseEmitter.event().name("comment-like-count").data(payload));
            } catch (IOException e) {
                postStreamEmitterRepository.remove(emitter);
            }
        }
    }

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

    public record PostCommentEvent(
            Long id,
            Long postId,
            Long parentId,
            Long writerId,
            String writerNickname,
            String writerProfileImageUrl,
            String content,
            boolean deleted,
            LocalDateTime createdAt,
            long likeCount,
            boolean likedByMe,
            List<PostCommentEvent> replies
    ) {
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
            long likeCount
    ) {
    }
}
