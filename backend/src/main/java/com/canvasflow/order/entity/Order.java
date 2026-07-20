package com.canvasflow.order.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문서. 결제 "전에" 서버가 만들어 두고, 결제가 끝나면 이 기록을 근거로 지급한다.
 *
 * 상태 컬럼을 따로 두지 않고 두 개의 id 로 진행 단계를 표현한다.
 *   paymentId == null  → 아직 결제 시도 안 함 (성공/실패는 payments 기록이 안다)
 *   ledgerId  == null  → 아직 지갑 반영 안 됨
 *   둘 다 채워짐        → 완료
 * 결제는 됐는데 ledgerId 가 비어 있는 주문 = 돈은 받았는데 지급이 안 된 건(복구 대상).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders",
        indexes = @Index(name = "idx_orders_user", columnList = "user_id"))
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 토스에 넘기는 주문번호. 서버가 발급하며 결제 식별 + 멱등 키로 쓴다. */
    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderPurpose purpose;

    /** 지급 대상 (구매할 글 id, 구독 티어 id 등). 충전은 대상이 없어 null. */
    @Column(name = "target_id")
    private Long targetId;

    /** 서버가 계산한 결제 금액(원). 승인 시 이 값을 쓰므로 프론트가 조작할 수 없다. */
    @Column(nullable = false)
    private long amount;

    /** 결제 기록(payments) id. 승인 시도 후 채워진다. */
    @Column(name = "payment_id")
    private Long paymentId;

    /** 지갑 원장(wallet_ledger) id. 지급까지 끝나면 채워진다. */
    @Column(name = "ledger_id")
    private Long ledgerId;

    public Order(String orderId, Long userId, OrderPurpose purpose, Long targetId, long amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.purpose = purpose;
        this.targetId = targetId;
        this.amount = amount;
    }

    public void linkPayment(Long paymentId) {
        this.paymentId = paymentId;
    }

    public void linkLedger(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    /** 지급까지 끝난 주문인가 */
    public boolean isFulfilled() {
        return this.ledgerId != null;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
