package com.canvasflow.order.dto;

/**
 * 결제 확정 요청. 토스 리다이렉트로 받은 paymentKey 만 보내면 된다.
 * 금액은 서버가 주문에 저장해 둔 값을 쓰므로 프론트가 보내지 않는다.
 */
public record OrderConfirmRequest(
        String paymentKey
) {
}
