package com.canvasflow.notification;

import com.canvasflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * notification 모듈이 다른 모듈에 노출하는 유일한 창구. 알림을 만들고 싶은 모듈은
 * (comment/like/post 등) notification.service.NotificationService를 직접 알 필요 없이 이것만 의존한다.
 */
@RequiredArgsConstructor
@Service
public class NotificationFacade {

    private final NotificationService notificationService;

    public void notify(
            Long receiverId,
            Long senderId,
            NotificationType type,
            NotificationTargetType targetType,
            Long targetId,
            String message) {
        notificationService.notify(receiverId, senderId, type, targetType, targetId, message);
    }
}
