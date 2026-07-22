package com.canvasflow.subscription.dto;

import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;

public record SubscriptionResponse(
        Long id,
        Long channelId,
        Long tierId,
        String tierName,
        SubscriptionStatus status
) {
    public static SubscriptionResponse from(Subscription subscription) {
        var tier = subscription.getTier();
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getChannelId(),
                tier.getId(),
                tier.getName(),
                subscription.getStatus()
        );
    }
}