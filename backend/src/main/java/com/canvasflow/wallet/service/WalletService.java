package com.canvasflow.wallet.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.wallet.entity.Wallet;
import com.canvasflow.wallet.entity.WalletLedger;
import com.canvasflow.wallet.repository.WalletLedgerRepository;
import com.canvasflow.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지갑 도메인 핵심 서비스. 충전/차감/잔액조회.
 *
 * 충전·차감은 반드시 트랜잭션 안에서 잔액 행을 비관적 잠금으로 잡고 처리한다.
 * (같은 사용자의 동시 요청이 잔액을 두 번 읽고 이중 사용하는 것을 막는다.)
 * 원장(WalletLedger)에는 모든 증감을 append-only 로 남긴다.
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletLedgerRepository ledgerRepository;

    /** 잔액 조회 (잠금 없이 빠르게). 지갑이 없으면 0. */
    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        return walletRepository.findById(userId)
                .map(Wallet::getBalance)
                .orElse(0L);
    }

    /**
     * 충전(+). PG 결제 승인이 끝난 뒤 호출되는 것을 전제로 한다(이 메서드는 잔액만 올린다).
     * @param refId 관련 결제 식별자 (없으면 null)
     */
    @Transactional
    public long charge(Long userId, long amount, Long refId) {
        if (amount <= 0) {
            throw new CanvasflowException(ErrorCode.WALLET_INVALID_AMOUNT);
        }
        Wallet wallet = lockOrCreate(userId);
        wallet.charge(amount);
        ledgerRepository.save(new WalletLedger(
                userId, amount, WalletLedger.Type.CHARGE, refId, wallet.getBalance()));
        return wallet.getBalance();
    }

    /**
     * 사용(-). 잔액이 부족하면 예외를 던지고 아무것도 차감하지 않는다.
     * @param type  PURCHASE | SUBSCRIPTION
     * @param refId 관련 구매/구독 식별자 (없으면 null)
     * @return 차감 후 잔액
     */
    @Transactional
    public long use(Long userId, long amount, WalletLedger.Type type, Long refId) {
        if (amount <= 0) {
            throw new CanvasflowException(ErrorCode.WALLET_INVALID_AMOUNT);
        }
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.WALLET_INSUFFICIENT_BALANCE));

        if (!wallet.tryUse(amount)) {
            throw new CanvasflowException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
        }
        ledgerRepository.save(new WalletLedger(
                userId, -amount, type, refId, wallet.getBalance()));
        return wallet.getBalance();
    }

    /** 잠금 조회 후 없으면 생성. (첫 충전 시 지갑 자동 개설) */
    private Wallet lockOrCreate(Long userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(userId)));
    }
}
