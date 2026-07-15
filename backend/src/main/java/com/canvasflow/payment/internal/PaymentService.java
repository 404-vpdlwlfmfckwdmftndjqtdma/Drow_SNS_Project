package com.canvasflow.payment.internal;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.payment.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
class PaymentService implements PaymentGateway {

    private final TossPaymentClient tossClient;
    private final PaymentRepository paymentRepository;

    PaymentService(TossPaymentClient tossClient, PaymentRepository paymentRepository) {
        this.tossClient = tossClient;
        this.paymentRepository = paymentRepository;
    }

    /** 기존 메서드 - PaymentController가 쓰던 것 그대로 유지 */
    @Transactional
    public PaymentResponse confirm(ConfirmRequest request) {
        return doConfirm(request.paymentKey(), request.orderId(), request.amount());
    }

    /** 신규 메서드 - 다른 모듈(subscription, purchase)이 쓰는 공개 창구 구현 */
    @Override
    @Transactional
    public void confirm(String paymentKey, String orderId, Long expectedAmount) {
        doConfirm(paymentKey, orderId, expectedAmount);
    }

    /** 공통 로직 - 기존 confirm 내용은 여기 */
    @Transactional
    public PaymentResponse doConfirm(String paymentKey, String orderId, Long amount) {
        // 이미 승인 처리된 결제면 중복 저장 방지 (멱등)
        if (paymentRepository.existsByPaymentKey(paymentKey)) {
            throw new CanvasflowException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        // ① 토스에 실제 승인 요청 (여기서 결제 확정)
        TossConfirmResponse toss;
        try {
            toss = tossClient.confirm(paymentKey, orderId, amount);
        } catch (RestClientResponseException e) {
            // e.getResponseBodyAsString()에 토스 실패 사유 JSON이 있음 - 로그로 남기기
            log.warn("토스 결제 승인 실패: {}", e.getResponseBodyAsString());
            throw new CanvasflowException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // ② 결과 저장
        PaymentEntity saved = paymentRepository.save(new PaymentEntity(
                toss.paymentKey(), toss.orderId(), toss.orderName(),
                toss.totalAmount(), toss.status(), toss.method(), toss.approvedAt()));

        // ③ 프론트에 결과 반환
        return new PaymentResponse(
                saved.getPaymentKey(), saved.getOrderId(), saved.getOrderName(),
                saved.getAmount(), saved.getStatus(), saved.getMethod(), saved.getApprovedAt());
    }
}
