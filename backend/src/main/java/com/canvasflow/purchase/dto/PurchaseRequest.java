package com.canvasflow.purchase.dto;

public record PurchaseRequest(
        String paymentKey,
        String orderId
) {
}
