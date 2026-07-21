package com.canvasflow.notification.service;

import com.canvasflow.notification.dto.NotificationResponse;
import com.canvasflow.notification.entity.Notification;
import com.canvasflow.notification.NotificationTargetType;
import com.canvasflow.notification.NotificationType;
import com.canvasflow.notification.repository.NotificationRepository;
import com.canvasflow.notification.sse.SseEmitterRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * 댓글/좋아요/팔로우/신규구독 발생 시 각 도메인 서비스에서 이 메서드를 호출해 알림을 저장한다.
 * (댓글 -> CommentService, 좋아요 -> LikeService, 팔로우 -> FollowService,
 *  신규 게시글 -> PostService, 신규 구독 -> SubscriptionService 에서 각각 TODO 로 연동 지점 표시됨)
 * 저장 직후 SSE로 연결돼 있는 수신자 브라우저에도 실시간으로 밀어준다 (연결 안 돼있으면 그냥 스킵 - 다음 접속 시 목록 조회로 확인).
 */
@RequiredArgsConstructor
@Service
public class NotificationService {

    // 프록시/브라우저가 무응답 커넥션을 끊는 걸 막기 위한 하트비트 주기
    private static final long HEARTBEAT_INTERVAL_MS = 30_000;
    // 클라이언트가 재연결(EventSource 기본 재시도)로 이어받을 수 있으니 넉넉하게 잡는다
    private static final long SUBSCRIBE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final NotificationRepository notificationRepository;
    private final UserFacade userFacade;
    private final SseEmitterRepository sseEmitterRepository;

    @Transactional
    public void notify(
            Long receiverId,
            Long senderId,
            NotificationType type,
            NotificationTargetType targetType,
            Long targetId,
            String message) {
        if (!userFacade.existsById(receiverId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        Notification notification = notificationRepository.save(Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .message(message)
                .build());

        sendToUser(receiverId, "notification", NotificationResponse.from(notification));
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SUBSCRIBE_TIMEOUT_MS);
        sseEmitterRepository.save(userId, emitter);
        emitter.onCompletion(() -> sseEmitterRepository.remove(userId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.remove(userId, emitter));
        emitter.onError(e -> sseEmitterRepository.remove(userId, emitter));

        try {
            // 연결 직후 더미 이벤트를 보내야 일부 프록시/브라우저가 커넥션을 바로 확정한다.
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            sseEmitterRepository.remove(userId, emitter);
        }
        return emitter;
    }

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        for (SseEmitter emitter : sseEmitterRepository.findAll()) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                emitter.complete();
            }
        }
    }

    private void sendToUser(Long userId, String eventName, Object data) {
        for (SseEmitter emitter : sseEmitterRepository.findByUserId(userId)) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                sseEmitterRepository.remove(userId, emitter);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE));
        if (!notification.getReceiverId().equals(userId)) {
            throw new CanvasflowException(ErrorCode.FORBIDDEN);
        }
        notification.markAsRead();
    }

    // 선택 삭제. receiverId까지 같이 걸어서 지우므로 남의 알림 id가 섞여 와도 그 부분만 조용히 무시된다
    // (전체를 막고 예외를 던지기보다, 내 것만 지우고 넘어가는 편이 UX상 자연스럽다고 판단).
    @Transactional
    public void deleteNotifications(List<Long> notificationIds, Long userId) {
        if (notificationIds.isEmpty()) {
            return;
        }
        notificationRepository.deleteByIdInAndReceiverId(notificationIds, userId);
    }
}
