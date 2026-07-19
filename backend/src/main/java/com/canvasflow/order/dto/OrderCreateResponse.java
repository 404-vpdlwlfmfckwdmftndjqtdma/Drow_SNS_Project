package com.canvasflow.order.dto;

import com.canvasflow.order.entity.OrderPurpose;

/**
 * 주문 생성 응답. 프론트는 이 orderId/amount 로 토스 결제창을 띄운다.
 * (금액을 프론트가 정하지 않고 서버가 내려주는 것이 핵심)
 */
public record OrderCreateResponse(
        String orderId,
        long amount,
        OrderPurpose purpose
) {
}
