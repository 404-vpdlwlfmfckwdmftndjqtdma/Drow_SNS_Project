package com.canvasflow.subscription.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    /** 유료 구독의 혜택 만료 시각. 무료 구독(tier=null)이면 null  */
    private LocalDateTime expiresAt;

    /**
     * 구독 등급. null 이면 등급 없는 기본 구독(무료 팔로우).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private SubscriptionTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Builder
    public Subscription(Long subscriberId, Long channelId, SubscriptionTier tier) {
        if (tier != null && !tier.getChannelId().equals(channelId)) {
            throw new IllegalArgumentException("해당 채널의 등급이 아닙니다.");
        }
        this.subscriberId = subscriberId;
        this.channelId = channelId;
        this.tier = tier;
        this.status = SubscriptionStatus.ACTIVE;
    }

    /** 현재 유효한 등급 레벨. 해지 상태거나 무료 구독이면 0 */
    public int effectiveLevel() {
        if (status != SubscriptionStatus.ACTIVE) return 0;
        return tier == null ? 0 : tier.getLevel();
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
    }

    public void changeTier(SubscriptionTier newTier) {
        if (newTier != null && !newTier.getChannelId().equals(this.channelId)) {
            throw new IllegalArgumentException("해당 채널의 등급이 아닙니다.");
        }
        this.tier = newTier;
    }

    /** 해지했던 구독 재활성화 (유니크 제약상 재구독은 기존 행 재사용) */
    public void reactivate(SubscriptionTier tier) {
        changeTier(tier);
        this.status = SubscriptionStatus.ACTIVE;
    }

    /** 혜택이 살아있는가 (월 정액권: 30일 비갱신) */
    public boolean isBenefitActive() {
        if (status != SubscriptionStatus.ACTIVE) return false;
        if (tier == null) return true;
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    public int effectivelevel() {
        if (!isBenefitActive()) return 0;
        return tier == null ? 0 : tier.getLevel();
    }

    /** 결재 완료 후 호출: 30일 이용권 시작/연장 */
    public void startPaidPeriod(SubscriptionTier tier) {
        changeTier(tier);
        this.status = SubscriptionStatus.ACTIVE;
        // 아직 기간이 남아있으면 연장(남은 날+30), 만료되어있으면 지금부터 30일
        LocalDateTime base = (expiresAt != null && expiresAt.isAfter(LocalDateTime.now()))
                ? expiresAt : LocalDateTime.now();
        this.expiresAt = base.plusDays(30);
    }

}
