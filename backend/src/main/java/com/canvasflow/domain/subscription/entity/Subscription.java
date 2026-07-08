package com.canvasflow.domain.subscription.entity;

import com.canvasflow.domain.user.entity.User;
import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 등급별 콘텐츠 잠금의 핵심 엔티티.
 * targetType=USER  -> targetId 는 User.id (작가 구독)
 * targetType=CHANNEL -> targetId 는 Channel.id (채널 구독)
 *
 * TODO: 구독 등급(tier)을 작성자가 자유롭게 정의할 수 있도록 별도 SubscriptionPlan 엔티티로
 *       확장 검토 (현재는 tier 를 문자열로 단순화).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"subscriber_id", "target_type", "target_id"}))
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 30)
    private String tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Builder
    public Subscription(User subscriber, SubscriptionTargetType targetType, Long targetId, String tier) {
        this.subscriber = subscriber;
        this.targetType = targetType;
        this.targetId = targetId;
        this.tier = tier;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
    }
}
