package com.canvasflow.subscription.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 채널 구독. (작가 개인 구독은 지원하지 않음 - 채널 단위로만 구독)
 * 구독 등급별 콘텐츠 잠금의 핵심 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_subscriber_channel",
                columnNames = {"subscriber_id", "channel_id"}))
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    /** 구독 혜택 만료 시각 */
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private SubscriptionTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Builder
    public Subscription(Long subscriberId, Long channelId, SubscriptionTier tier) {
        Objects.requireNonNull(tier, "구독 등급은 필수입니다.");
        if (!tier.getChannelId().equals(channelId)) {
            throw new IllegalArgumentException("해당 채널의 등급이 아닙니다.");
        }
        this.subscriberId = subscriberId;
        this.channelId = channelId;
        this.tier = tier;
        this.status = SubscriptionStatus.ACTIVE;
    }

    /**
     * 유료 구독 혜택이 지금 유효한가 (= 블러가 풀리는가).
     *
     * 반드시 isBenefitActive()를 거쳐야 한다 - status만 보면 30일 이용권이 끝난 뒤에도
     * (해지하지 않는 한 status는 계속 ACTIVE라) 혜택이 영구히 유지되는 버그가 된다.
     */
    public boolean hasPaidBenefit() {
        return isBenefitActive();
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
    }

    public void changeTier(SubscriptionTier newTier) {
        Objects.requireNonNull(newTier, "구독 등급은 필수입니다.");
        if (!newTier.getChannelId().equals(this.channelId)) {
            throw new IllegalArgumentException("해당 채널의 등급이 아닙니다.");
        }
        this.tier = newTier;
    }

    /** 혜택이 살아있는가 (월 정액권: 30일 비갱신) */
    public boolean isBenefitActive() {
        return status == SubscriptionStatus.ACTIVE
                && tier != null
                && expiresAt != null
                && expiresAt.isAfter(LocalDateTime.now());
    }

    /** 결제 완료 후 호출: 30일 이용권 시작/연장 */
    public void startPaidPeriod(SubscriptionTier tier) {
        changeTier(tier);
        this.status = SubscriptionStatus.ACTIVE;
        // 아직 기간이 남아있으면 연장(남은 날+30), 만료되어있으면 지금부터 30일
        LocalDateTime base = (expiresAt != null && expiresAt.isAfter(LocalDateTime.now()))
                ? expiresAt : LocalDateTime.now();
        this.expiresAt = base.plusDays(30);
    }

}
