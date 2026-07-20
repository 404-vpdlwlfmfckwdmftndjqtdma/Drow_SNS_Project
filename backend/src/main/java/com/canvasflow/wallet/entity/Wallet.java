package com.canvasflow.wallet.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 지갑 잔액 스냅샷. user_id 당 1행.
 *
 * 실제 증감 내역은 wallet_ledger 에 append-only 로 쌓이고,
 * 이 행은 "현재 잔액"을 빠르게 읽고 차감 시 비관적 잠금(FOR UPDATE)을 걸 대상이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "wallets")
public class Wallet extends BaseTimeEntity {

    /** user_id 를 그대로 PK 로 사용 (지갑은 사용자당 1개) */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** 현재 잔액(원). 0 이상 유지. */
    @Column(nullable = false)
    private long balance;

    public Wallet(Long userId) {
        this.userId = userId;
        this.balance = 0L;
    }

    /** 충전: 잔액 증가 */
    public void charge(long amount) {
        this.balance += amount;
    }

    /** 사용: 잔액 차감. 부족하면 false 를 돌려주고 잔액은 그대로 둔다. */
    public boolean tryUse(long amount) {
        if (this.balance < amount) {
            return false;
        }
        this.balance -= amount;
        return true;
    }
}
