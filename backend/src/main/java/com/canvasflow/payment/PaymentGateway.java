package com.canvasflow.payment;

/** 다른 모듈이 결재 승인은 요청하는 공개 창구 */
public interface PaymentGateway {

    /**
     * 토스에 결제 승인을 요청한다.
     * 승인 요청 "전에" 시도 기록을 독립 트랜잭션으로 남기므로,
     * 응답을 못 받아도(타임아웃) 추적할 기록이 남는다.
     *
     * @param expectedAmount 서버가 계산한 금액 (원 단위).
     *                       토스는 경재창에서 인증된 금액과 다르면 승인을 거부하므로
     *                       반드시 서버 가격을 넘겨야 조작이 차단됨.
     * @return 결제 기록(payments)의 id. 호출한 쪽이 자기 주문에 연결해 두면 추적이 쉬워진다.
     */
    Long confirm(String paymentKey, String orderId, Long expectedAmount);

}
