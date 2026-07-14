package com.canvasflow.like.service;

import com.canvasflow.comment.entity.Comment;
import com.canvasflow.comment.repository.CommentRepository;
import com.canvasflow.comment.sse.CommentEmitterRepository;
import com.canvasflow.like.dto.CommentLikeCountEvent;
import com.canvasflow.like.dto.LikeResponse;
import com.canvasflow.like.entity.Like;
import com.canvasflow.like.entity.LikeTargetType;
import com.canvasflow.like.repository.LikeRepository;
import com.canvasflow.like.sse.LikeEmitterRepository;
import com.canvasflow.notification.entity.NotificationTargetType;
import com.canvasflow.notification.entity.NotificationType;
import com.canvasflow.notification.service.NotificationService;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class LikeService {

    // 프록시/브라우저가 무응답 커넥션을 끊는 걸 막기 위한 하트비트 주기
    private static final long HEARTBEAT_INTERVAL_MS = 30_000;
    // 클라이언트가 재연결(EventSource 기본 재시도)로 이어받을 수 있으니 넉넉하게 잡는다
    private static final long SUBSCRIBE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final LikeEmitterRepository likeEmitterRepository;
    private final CommentEmitterRepository commentEmitterRepository;

    private final UserFacade userFacade;
    // TODO: NotificationService 주입 -> 좋아요 발생 시 대상 작성자에게 알림

    @Transactional
    public LikeResponse like(Long userId, LikeTargetType targetType, Long targetId) {
        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)) {
            throw new CanvasflowException(ErrorCode.ALREADY_LIKED);
        }
        String likerNickname = userFacade.getNicknameOrThrow(userId);
        likeRepository.save(Like.builder().userId(userId).targetType(targetType).targetId(targetId).build());
        notifyLiked(userId, likerNickname, targetType, targetId);
        long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        broadcastCount(targetType, targetId, likeCount);
        broadcastCommentLikeIfNeeded(targetType, targetId, likeCount);
        return new LikeResponse(true, likeCount);
    }

    // 알림 저장은 좋아요 자체를 막으면 안 되는 부가 효과라 예외를 삼킨다.
    private void notifyLiked(Long likerId, String likerNickname, LikeTargetType targetType, Long targetId) {
        try {
            if (targetType == LikeTargetType.POST) {
                postRepository.findById(targetId).ifPresent(post -> {
                    if (!post.getUserId().equals(likerId)) {
                        notificationService.notify(
                                post.getUserId(), likerId, NotificationType.LIKE,
                                NotificationTargetType.POST, targetId,
                                likerNickname + "님이 회원님의 게시글을 좋아합니다.");
                    }
                });
            } else {
                commentRepository.findById(targetId).ifPresent(comment -> {
                    if (!comment.getWriterId().equals(likerId)) {
                        notificationService.notify(
                                comment.getWriterId(), likerId, NotificationType.LIKE,
                                NotificationTargetType.COMMENT, targetId,
                                likerNickname + "님이 회원님의 댓글을 좋아합니다.");
                    }
                });
            }
        } catch (Exception e) {
            // 알림 저장 실패는 무시 - 다음 접속 시 목록 조회로 확인 가능
        }
    }

    @Transactional
    public LikeResponse unlike(Long userId, LikeTargetType targetType, Long targetId) {
        Like like = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.LIKE_NOT_FOUND));
        likeRepository.delete(like);
        long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        broadcastCount(targetType, targetId, likeCount);
        broadcastCommentLikeIfNeeded(targetType, targetId, likeCount);
        return new LikeResponse(false, likeCount);
    }

    // 로그인 없이도(userId == null) 개수 조회는 가능 - 이 경우 liked는 항상 false.
    @Transactional(readOnly = true)
    public LikeResponse getStatus(Long userId, LikeTargetType targetType, Long targetId) {
        long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        boolean liked = userId != null
                && likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        return new LikeResponse(liked, likeCount);
    }

    // 로그인 없이도(익명 열람) 개수 브로드캐스트를 받을 수 있도록 인증 없이 구독을 허용한다.
    public SseEmitter subscribe(LikeTargetType targetType, Long targetId) {
        SseEmitter emitter = new SseEmitter(SUBSCRIBE_TIMEOUT_MS);
        likeEmitterRepository.save(targetType, targetId, emitter);
        emitter.onCompletion(() -> likeEmitterRepository.remove(targetType, targetId, emitter));
        emitter.onTimeout(() -> likeEmitterRepository.remove(targetType, targetId, emitter));
        emitter.onError(e -> likeEmitterRepository.remove(targetType, targetId, emitter));

        try {
            // 연결 직후 더미 이벤트를 보내야 일부 프록시/브라우저가 커넥션을 바로 확정한다.
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            likeEmitterRepository.remove(targetType, targetId, emitter);
        }
        return emitter;
    }

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        for (SseEmitter emitter : likeEmitterRepository.findAll()) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                emitter.complete();
            }
        }
    }

    private void broadcastCount(LikeTargetType targetType, Long targetId, long likeCount) {
        for (SseEmitter emitter : likeEmitterRepository.findByTarget(targetType, targetId)) {
            try {
                emitter.send(SseEmitter.event().name("like-count").data(likeCount));
            } catch (IOException e) {
                likeEmitterRepository.remove(targetType, targetId, emitter);
            }
        }
    }

    // 댓글 좋아요는 댓글마다 별도 SSE 연결을 열지 않고, 이미 열려있는 게시글의 댓글 채널에 얹어서 보낸다
    // (브라우저는 오리진당 동시 연결 수(HTTP/1.1 기준 6개)에 제한이 있어, 댓글 개수만큼 연결을 열면 금방 바닥난다).
    private void broadcastCommentLikeIfNeeded(LikeTargetType targetType, Long targetId, long likeCount) {
        if (targetType != LikeTargetType.COMMENT) {
            return;
        }
        commentRepository.findById(targetId).map(Comment::getPostId).ifPresent(postId -> {
            for (SseEmitter emitter : commentEmitterRepository.findByPostId(postId)) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("comment-like-count")
                            .data(new CommentLikeCountEvent(targetId, likeCount)));
                } catch (IOException e) {
                    commentEmitterRepository.remove(postId, emitter);
                }
            }
        });
    }
}
