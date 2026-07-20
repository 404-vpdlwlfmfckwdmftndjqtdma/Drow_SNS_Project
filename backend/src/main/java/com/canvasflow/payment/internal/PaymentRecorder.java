package com.canvasflow.payment.internal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 결제 시도 기록을 "독립 트랜잭션"으로 남긴다. (REQUIRES_NEW)
 *
 * 호출자(order 등)의 트랜잭션이 나중에 롤백되더라도 결제 시도/결과 기록은 남아야 한다.
 * 그래야 "승인은 됐는데 뒤처리가 실패한 건"을 나중에 찾아 복구할 수 있다.
 */
@Component
class PaymentRecorder {

    private final PaymentRepository paymentRepository;

    PaymentRecorder(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /** 승인 요청 직전 기록. 같은 paymentKey 기록이 있으면 그것을 재사용한다(재시도). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentEntity beginOrReuse(String paymentKey, String orderId, Long amount) {
        Optional<PaymentEntity> existing = paymentRepository.findByPaymentKey(paymentKey);
        return existing.orElseGet(() ->
                paymentRepository.save(new PaymentEntity(paymentKey, orderId, amount)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long paymentId, TossConfirmResponse toss) {
        paymentRepository.findById(paymentId).ifPresent(p ->
                p.markSuccess(toss.orderName(), toss.method(), toss.approvedAt(), toss.status()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long paymentId, String reason) {
        paymentRepository.findById(paymentId).ifPresent(p -> p.markFailed(reason));
    }
}
