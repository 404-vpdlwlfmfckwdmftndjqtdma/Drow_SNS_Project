package com.canvasflow.purchase.dto;

import com.canvasflow.purchase.entity.PostPurchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        Long postId,
        BigDecimal price,
        LocalDateTime purchasedAt
) {
    public static PurchaseResponse from(PostPurchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getPostId(),
                purchase.getPrice(),
                purchase.getCreatedAt()   // BaseTimeEntity의 생성 시각
        );
    }
}