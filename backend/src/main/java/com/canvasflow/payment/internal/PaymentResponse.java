package com.canvasflow.payment.internal;

/** 승인 완료 후 프론트에 돌려줄 결제 결과. */
public record PaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        Long amount,
        String status,
        String method,
        String approvedAt
) {
}
