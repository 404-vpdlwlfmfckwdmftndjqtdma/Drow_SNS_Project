package com.canvasflow.payment.internal;

/** 프론트 success 페이지가 보내는 승인 요청 (토스 리다이렉트로 받은 값). */
public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
