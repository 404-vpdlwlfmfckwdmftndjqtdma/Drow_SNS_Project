package com.canvasflow.payment.internal;

/**
 * 토스 승인 API 응답 중 필요한 필드만 매핑.
 * (응답엔 필드가 더 많지만 Spring Boot 기본 설정이 미지의 필드는 무시)
 */
public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String orderName,
        Long totalAmount,   // 토스는 amount 가 아니라 totalAmount 로 내려줌
        String status,
        String method,
        String approvedAt
) {
}
