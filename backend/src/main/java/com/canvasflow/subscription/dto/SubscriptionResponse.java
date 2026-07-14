package com.canvasflow.subscription.dto;

import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;

public record SubscriptionResponse(
        Long id,
        Long channelId,
        Long tierId,        // 무료 구독이면 null
        String tierName,    // 무료 구독이면 null
        SubscriptionStatus status
) {
    public static SubscriptionResponse from(Subscription subscription) {
        var tier = subscription.getTier();
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getChannelId(),
                tier == null ? null : tier.getId(),
                tier == null ? null : tier.getName(),
                subscription.getStatus()
        );
    }
}