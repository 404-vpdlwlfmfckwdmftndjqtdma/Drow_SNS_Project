package com.canvasflow.like.service;

import com.canvasflow.like.LikeTargetType;
import com.canvasflow.like.TargetLikedEvent;
import com.canvasflow.like.dto.LikeResponse;
import com.canvasflow.like.entity.Like;
import com.canvasflow.like.repository.LikeRepository;
import com.canvasflow.like.sse.LikeEmitterRepository;
import com.canvasflow.notification.NotificationFacade;
import com.canvasflow.notification.NotificationTargetType;
import com.canvasflow.notification.NotificationType;
import com.canvasflow.post.PostReader;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 좋아요 대상이 게시글이면 post 모듈을 직접 참조해 알림을 만든다(post는 like/comment에 의존하지 않으므로
 * 순환이 안 생김). 대상이 댓글이면 comment 모듈을 직접 참조하지 않고 TargetLikedEvent만 발행한다
 * (comment도 이 모듈의 LikeReader를 의존하므로, 직접 참조하면 comment<->like 순환 의존이 생기기 때문).
 */
@RequiredArgsConstructor
@Service
public class LikeService {

    // 프록시/브라우저가 무응답 커넥션을 끊는 걸 막기 위한 하트비트 주기
    private static final long HEARTBEAT_INTERVAL_MS = 30_000;
    // 클라이언트가 재연결(EventSource 기본 재시도)로 이어받을 수 있으니 넉넉하게 잡는다
    private static final long SUBSCRIBE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final LikeRepository likeRepository;
    private final PostReader postReader;
    private final NotificationFacade notificationFacade;
    private final LikeEmitterRepository likeEmitterRepository;
    private final UserFacade userFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LikeResponse like(Long userId, LikeTargetType targetType, Long targetId) {
        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)) {
            throw new CanvasflowException(ErrorCode.ALREADY_LIKED);
        }
        String likerNickname = userFacade.getNicknameOrThrow(userId);
        likeRepository.save(Like.builder().userId(userId).targetType(targetType).targetId(targetId).build());
        long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        notifyAndPublish(userId, targetType, targetId, true, likeCount);
        broadcastCount(targetType, targetId, likeCount);
        return new LikeResponse(true, likeCount);
    }

    @Transactional
    public LikeResponse unlike(Long userId, LikeTargetType targetType, Long targetId) {
        Like like = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.LIKE_NOT_FOUND));
        likeRepository.delete(like);
        long likeCount = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        notifyAndPublish(userId, targetType, targetId, false, likeCount);
        broadcastCount(targetType, targetId, likeCount);
        return new LikeResponse(false, likeCount);
    }

    // 댓글 대상: comment 모듈을 직접 부르지 않고 이벤트만 발행한다 (누가 구독하는지 모름 - Pub/Sub).
    // 게시글 대상: post는 like/comment에 의존하지 않아 순환 걱정이 없으므로 그냥 직접 조회+알림한다.
    private void notifyAndPublish(Long likerId, LikeTargetType targetType, Long targetId, boolean liked, long likeCount) {
        if (targetType == LikeTargetType.COMMENT) {
            String likerNickname = liked ? userFacade.findNicknameById(likerId) : null;
            eventPublisher.publishEvent(new TargetLikedEvent(targetId, likerId, likerNickname, liked, likeCount));
            return;
        }
        if (!liked) {
            return; // 게시글 좋아요 취소는 알림 없음
        }
        try {
            String likerNickname = userFacade.getNicknameOrThrow(likerId);
            postReader.getPostInfo(targetId).ifPresent(postInfo -> {
                if (!postInfo.authorId().equals(likerId)) {
                    notificationFacade.notify(
                            postInfo.authorId(), likerId, NotificationType.LIKE,
                            NotificationTargetType.POST, targetId,
                            likerNickname + "님이 회원님의 게시글을 좋아합니다.");
                }
            });
        } catch (Exception e) {
            // 알림 저장 실패는 좋아요 자체를 막으면 안 되는 부가 효과라 무시
        }
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
}
