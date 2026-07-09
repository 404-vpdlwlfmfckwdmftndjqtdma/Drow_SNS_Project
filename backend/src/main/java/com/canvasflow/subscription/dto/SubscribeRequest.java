package com.canvasflow.subscription.dto;

import com.canvasflow.subscription.entity.SubscriptionTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        @NotNull SubscriptionTargetType targetType,
        @NotNull Long targetId,
        @NotBlank String tier
) {
}
