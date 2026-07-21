package com.canvasflow.purchase.dto;

import com.canvasflow.purchase.entity.PurchaseItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        Long postId,
        String capability,
        BigDecimal price,
        LocalDateTime purchasedAt
) {
    public static PurchaseResponse from(PurchaseItem item) {
        return new PurchaseResponse(
                item.getId(),
                item.getPostId(),
                item.getCapability(),
                item.getPrice(),
                item.getCreatedAt()   // BaseTimeEntity의 생성 시각
        );
    }
}
