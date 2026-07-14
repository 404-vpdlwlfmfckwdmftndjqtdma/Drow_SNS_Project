package com.canvasflow.subscription.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 채널이 정의하는 구독 등급.
 * 예) 채널 7: [팬(level 1, 3000원), 서포터(level 2, 5000원), VIP(level 3, 10000원)]
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "subscription_tiers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_channel_level",
                columnNames = {"channel_id", "level"})
)
public class SubscriptionTier extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(nullable = false, length = 30)
    private String name;                // 등급 이름 (팬, 서포터, VIP ...)

    /** 숫자가 클수록 상위 등급. 판정: 구독자 level >= 게시물 요구 level */
    @Column(nullable = false)
    private int level;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal monthlyPrice;    // 월 구독료

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private boolean deleted;            // 구독자가 남아있을 수 있으므로 소프트 삭제

    @Builder
    public SubscriptionTier(Long channelId, String name, int level,
                            BigDecimal monthlyPrice, String description) {
        this.channelId = channelId;
        this.name = name;
        this.level = level;
        this.monthlyPrice = monthlyPrice;
        this.description = description;
        this.deleted = false;
    }

    public void update(String name, BigDecimal monthlyPrice, String description) {
        this.name = name;
        this.monthlyPrice = monthlyPrice;
        this.description = description;
    }

    public void delete() {
        this.deleted = true;
    }
}