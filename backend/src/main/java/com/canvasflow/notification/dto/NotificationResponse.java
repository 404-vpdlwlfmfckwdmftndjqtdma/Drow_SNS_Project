package com.canvasflow.notification.dto;

import com.canvasflow.notification.entity.Notification;
import com.canvasflow.notification.entity.NotificationTargetType;
import com.canvasflow.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long senderId,
        NotificationType type,
        NotificationTargetType targetType,
        Long targetId,
        String message,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getSenderId(),
                notification.getType(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
