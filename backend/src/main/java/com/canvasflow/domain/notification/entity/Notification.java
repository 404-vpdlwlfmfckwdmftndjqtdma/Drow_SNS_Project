package com.canvasflow.domain.notification.entity;

import com.canvasflow.domain.user.entity.User;
import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notifications")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 300)
    private String message;

    // 알림 클릭 시 이동할 대상 (postId, userId 등) - type 에 따라 의미가 달라짐
    private Long relatedId;

    @Column(nullable = false)
    private boolean isRead;

    @Builder
    public Notification(User receiver, NotificationType type, String message, Long relatedId) {
        this.receiver = receiver;
        this.type = type;
        this.message = message;
        this.relatedId = relatedId;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
