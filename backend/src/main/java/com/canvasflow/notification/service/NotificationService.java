package com.canvasflow.notification.service;

import com.canvasflow.notification.entity.Notification;
import com.canvasflow.notification.entity.NotificationType;
import com.canvasflow.notification.repository.NotificationRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글/좋아요/팔로우/신규구독 발생 시 각 도메인 서비스에서 이 메서드를 호출해 알림을 저장한다.
 * (댓글 -> CommentService, 좋아요 -> LikeService, 팔로우 -> FollowService,
 *  신규 게시글 -> PostService, 신규 구독 -> SubscriptionService 에서 각각 TODO 로 연동 지점 표시됨)
 */
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserFacade userFacade;

    @Transactional
    public void notify(Long receiverId, NotificationType type, String message, Long relatedId) {
        if (!userFacade.existsById(receiverId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        notificationRepository.save(Notification.builder()
                .receiverId(receiverId)
                .type(type)
                .message(message)
                .relatedId(relatedId)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE));
        // TODO: notification.getReceiver().getId().equals(userId) 검증
        notification.markAsRead();
    }
}
