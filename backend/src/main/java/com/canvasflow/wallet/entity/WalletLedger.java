package com.canvasflow.wallet.entity;

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
 * 지갑 거래 원장. append-only — 한 번 쌓이면 수정/삭제하지 않는다.
 * 정정이 필요하면 반대 부호의 거래를 새로 넣는다.
 *
 * amount: 충전이면 +, 사용이면 -.
 * balanceAfter: 이 거래 직후 잔액 스냅샷 (검산·추적용).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "wallet_ledger",
        indexes = @Index(name = "idx_wl_user", columnList = "user_id, id"))
public class WalletLedger extends BaseTimeEntity {

    public enum Type {
        CHARGE,        // 충전(+)
        PURCHASE,      // 개별 구매 사용(-)
        SUBSCRIPTION,  // 구독 사용(-)
        REFUND         // 환불(+)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 증감액(원). 충전 +, 사용 -. */
    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    /** 관련 구매/구독/결제 식별자 (없으면 null) */
    @Column(name = "ref_id")
    private Long refId;

    /** 이 거래 직후 잔액 */
    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    public WalletLedger(Long userId, long amount, Type type, Long refId, long balanceAfter) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.refId = refId;
        this.balanceAfter = balanceAfter;
    }
}
