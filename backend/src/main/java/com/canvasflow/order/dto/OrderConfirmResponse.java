package com.canvasflow.order.dto;

import com.canvasflow.order.entity.OrderPurpose;

/**
 * 결제 확정 응답.
 * @param alreadyProcessed 이미 처리된 주문을 다시 확정 요청한 경우(새로고침 등) true
 */
public record OrderConfirmResponse(
        String orderId,
        OrderPurpose purpose,
        long amount,
        long balance,
        boolean alreadyProcessed
) {
}
