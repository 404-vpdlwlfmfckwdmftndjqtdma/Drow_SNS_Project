package com.canvasflow.payment.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 결제 시도 기록. paymentKey 가 결제 1건의 고유 식별자.
 *
 * 승인 요청을 "보내기 전에" REQUESTED 로 먼저 저장하고, 응답이 오면 SUCCESS/FAILED 로 갱신한다.
 * 요청은 나갔는데 응답을 못 받은 경우(타임아웃)에도 시도 기록이 남아야 추적할 수 있기 때문이다.
 */
@Entity
@Table(name = "payments")
public class PaymentEntity {

    /** 우리 쪽 진행 상태 (토스가 준 상태는 tossStatus 에 따로 보관) */
    public enum Status { REQUESTED, SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private String orderId;

    /** 승인 요청 금액 (서버가 계산한 값) */
    private Long amount;

    /** REQUESTED → SUCCESS | FAILED */
    @Column(nullable = false, length = 20)
    private String status;

    // --- 승인 성공 시 채워지는 값들 ---
    private String orderName;
    private String method;
    private String approvedAt;

    /** 토스가 준 원본 상태 (예: DONE) */
    private String tossStatus;

    /** 승인 실패 사유 */
    @Column(length = 500)
    private String failReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected PaymentEntity() {
    }

    /** 승인 요청 직전 생성. 아직 결과는 모른다. */
    public PaymentEntity(String paymentKey, String orderId, Long amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.status = Status.REQUESTED.name();
        this.createdAt = LocalDateTime.now();
    }

    /** 승인 성공 결과 반영 */
    public void markSuccess(String orderName, String method, String approvedAt, String tossStatus) {
        this.status = Status.SUCCESS.name();
        this.orderName = orderName;
        this.method = method;
        this.approvedAt = approvedAt;
        this.tossStatus = tossStatus;
        this.failReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    /** 승인 실패 결과 반영 */
    public void markFailed(String reason) {
        this.status = Status.FAILED.name();
        this.failReason = reason != null && reason.length() > 500 ? reason.substring(0, 500) : reason;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return Status.SUCCESS.name().equals(this.status);
    }

    public Long getId() { return id; }
    public String getPaymentKey() { return paymentKey; }
    public String getOrderId() { return orderId; }
    public String getOrderName() { return orderName; }
    public Long getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getMethod() { return method; }
    public String getApprovedAt() { return approvedAt; }
    public String getTossStatus() { return tossStatus; }
    public String getFailReason() { return failReason; }
}
