package com.canvasflow.notification.entity;

import com.canvasflow.notification.NotificationTargetType;
import com.canvasflow.notification.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림은 생성된 후 내용이 바뀌지 않으므로(읽음 처리만 발생) BaseTimeEntity(updatedAt 포함)를 쓰지 않고
 * createdAt만 직접 갖는다 - 테이블에도 updated_at 컬럼이 없다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    // 알림을 유발한 사용자. 시스템 알림(공지 등)에는 없을 수 있어 null 허용.
    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    // target_type/target_id로 알림이 가리키는 대상(게시글/댓글)을 참조한다 - FK는 걸지 않는다(다른 도메인 소유 데이터).
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private NotificationTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // 미리 생성해둔 알림 문구. null이면 조회 시 type/targetType 등으로 조합해서 보여준다.
    @Column(length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(
            Long receiverId,
            Long senderId,
            NotificationType type,
            NotificationTargetType targetType,
            Long targetId,
            String message) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.type = type;
        this.targetType = targetType;
        this.targetId = targetId;
        this.message = message;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
