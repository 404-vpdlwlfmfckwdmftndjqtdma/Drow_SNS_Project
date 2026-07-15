package com.canvasflow.subscription.dto;

import com.canvasflow.subscription.entity.SubscriptionTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        Long tierId,          // null 이면 무료 구독(팔로우),
        String paymentKey,    // 유료일 때만
        String orderId        // 유료일 때만
) {
}