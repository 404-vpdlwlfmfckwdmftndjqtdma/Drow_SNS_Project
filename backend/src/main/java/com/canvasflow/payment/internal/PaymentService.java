package com.canvasflow.payment.internal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PaymentService {

    private final TossPaymentClient tossClient;
    private final PaymentRepository paymentRepository;

    PaymentService(TossPaymentClient tossClient, PaymentRepository paymentRepository) {
        this.tossClient = tossClient;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse confirm(ConfirmRequest request) {
        // 이미 승인 처리된 결제면 중복 저장 방지 (멱등)
        if (paymentRepository.existsByPaymentKey(request.paymentKey())) {
            throw new IllegalStateException("이미 처리된 결제입니다: " + request.paymentKey());
        }

        // ① 토스에 실제 승인 요청 (여기서 결제 확정)
        TossConfirmResponse toss = tossClient.confirm(
                request.paymentKey(), request.orderId(), request.amount());

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
