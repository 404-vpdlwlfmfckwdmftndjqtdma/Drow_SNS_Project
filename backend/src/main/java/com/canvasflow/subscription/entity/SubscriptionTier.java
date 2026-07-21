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
/*
 * 실제 DB에는 "삭제되지 않은 것들 중에서만" 이름 중복을 막는 부분 유니크 인덱스가 걸려 있다:
 *   CREATE UNIQUE INDEX uk_channel_tier_name
 *       ON subscription_tiers (channel_id, name) WHERE deleted = false;
 * JPA의 @UniqueConstraint로는 조건부 인덱스를 표현할 수 없어 여기 선언하지 않는다.
 * (선언하면 지웠던 이름을 다시 쓸 수 없게 되어 소프트 삭제와 충돌한다.)
 */
@Table(name = "subscription_tiers")
public class SubscriptionTier extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(nullable = false, length = 30)
    private String name;                // 상품 이름 (팬, 서포터, VIP ...)

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal monthlyPrice;    // 월 구독료

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private boolean deleted;            // 구독자가 남아있을 수 있으므로 소프트 삭제

    @Builder
    public SubscriptionTier(Long channelId, String name,
                            BigDecimal monthlyPrice, String description) {
        this.channelId = channelId;
        this.name = name;
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