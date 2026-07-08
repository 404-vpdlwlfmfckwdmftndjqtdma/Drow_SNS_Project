package com.canvasflow.domain.subscription.dto;

import com.canvasflow.domain.subscription.entity.Subscription;
import com.canvasflow.domain.subscription.entity.SubscriptionStatus;
import com.canvasflow.domain.subscription.entity.SubscriptionTargetType;

public record SubscriptionResponse(
        Long id,
        SubscriptionTargetType targetType,
        Long targetId,
        String tier,
        SubscriptionStatus status
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getTargetType(),
                subscription.getTargetId(),
                subscription.getTier(),
                subscription.getStatus()
        );
    }
}
