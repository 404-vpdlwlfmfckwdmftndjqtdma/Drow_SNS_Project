package com.canvasflow.payment.internal;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.payment.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

/**
 * 결제 승인 처리.
 *
 * 순서가 중요하다: "기록 → 승인 요청 → 결과 갱신".
 * 기록은 PaymentRecorder 가 독립 트랜잭션(REQUIRES_NEW)으로 남기므로,
 * 호출자(order)의 트랜잭션이 롤백돼도 "승인은 됐다"는 사실은 보존된다.
 */
@Slf4j
@Service
class PaymentService implements PaymentGateway {

    private final TossPaymentClient tossClient;
    private final PaymentRecorder recorder;

    PaymentService(TossPaymentClient tossClient, PaymentRecorder recorder) {
        this.tossClient = tossClient;
        this.recorder = recorder;
    }

    /** 기존 메서드 - PaymentController가 쓰던 것 그대로 유지 */
    public PaymentResponse confirm(ConfirmRequest request) {
        return doConfirm(request.paymentKey(), request.orderId(), request.amount()).response();
    }

    /** 공개 창구 구현 - order 등 다른 모듈이 사용. 결제 기록 id 를 돌려준다. */
    @Override
    public Long confirm(String paymentKey, String orderId, Long expectedAmount) {
        return doConfirm(paymentKey, orderId, expectedAmount).paymentId();
    }

    /**
     * 트랜잭션을 걸지 않는다. 각 기록은 PaymentRecorder 가 독립 트랜잭션으로 처리하고,
     * 토스 호출(외부)은 어차피 롤백 대상이 아니기 때문이다.
     */
    private Result doConfirm(String paymentKey, String orderId, Long amount) {
        // ① 승인 요청 전에 시도 기록부터 남긴다 (같은 키 재시도면 기존 기록 재사용)
        PaymentEntity record = recorder.beginOrReuse(paymentKey, orderId, amount);

        // 이미 승인 성공한 건이면 다시 승인하지 않는다 (멱등)
        if (record.isSuccess()) {
            return new Result(record.getId(), toResponse(record));
        }

        // ② 토스에 실제 승인 요청 (여기서 결제 확정)
        TossConfirmResponse toss;
        try {
            toss = tossClient.confirm(paymentKey, orderId, amount);
        } catch (RestClientResponseException e) {
            // e.getResponseBodyAsString()에 토스 실패 사유 JSON이 있음
            log.warn("토스 결제 승인 실패: {}", e.getResponseBodyAsString());
            recorder.markFailed(record.getId(), e.getResponseBodyAsString());
            throw new CanvasflowException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // ③ 성공 결과 반영
        recorder.markSuccess(record.getId(), toss);

        return new Result(record.getId(), new PaymentResponse(
                toss.paymentKey(), toss.orderId(), toss.orderName(),
                toss.totalAmount(), toss.status(), toss.method(), toss.approvedAt()));
    }

    private PaymentResponse toResponse(PaymentEntity p) {
        return new PaymentResponse(
                p.getPaymentKey(), p.getOrderId(), p.getOrderName(),
                p.getAmount(), p.getTossStatus(), p.getMethod(), p.getApprovedAt());
    }

    private record Result(Long paymentId, PaymentResponse response) {
    }
}
