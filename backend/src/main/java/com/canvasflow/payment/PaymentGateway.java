package com.canvasflow.payment;

/** 다른 모듈이 결재 승인은 요청하는 공개 창구 */
public interface PaymentGateway {

    /**
     * @param expectedAmount 서버가 계산한 금액 (원 단위).
     *                       토스는 경재창에서 인증된 금액과 다르면 승인을 거부하므로
     *                       반드시 서버 가격을 넘겨야 조작이 차단됨.
     */
    void confirm(String paymentKey, String orderId, Long expectedAmount);

}
