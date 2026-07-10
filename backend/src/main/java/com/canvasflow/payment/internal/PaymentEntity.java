package com.canvasflow.payment.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** 결제 결과 저장 테이블. paymentKey 가 결제 1건의 고유 식별자. */
@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private String orderId;

    private String orderName;
    private Long amount;
    private String status;
    private String method;
    private String approvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PaymentEntity() {
    }

    public PaymentEntity(String paymentKey, String orderId, String orderName,
                         Long amount, String status, String method, String approvedAt) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.approvedAt = approvedAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPaymentKey() { return paymentKey; }
    public String getOrderId() { return orderId; }
    public String getOrderName() { return orderName; }
    public Long getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getMethod() { return method; }
    public String getApprovedAt() { return approvedAt; }
}
