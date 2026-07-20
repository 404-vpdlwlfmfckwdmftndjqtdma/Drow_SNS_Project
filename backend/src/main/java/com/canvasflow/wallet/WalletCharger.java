package com.canvasflow.wallet;

import com.canvasflow.wallet.entity.WalletLedger;
import com.canvasflow.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * [wallet 모듈의 공개 창구 - 충전/차감 명령]
 *
 * order 모듈이 결제 완료 후 잔액을 올리거나, 상품 지급 시 잔액을 내릴 때 쓴다.
 * 잔액 조회만 필요하면 WalletReader 를 쓴다.
 */
@Service
@RequiredArgsConstructor
public class WalletCharger {

    private final WalletService walletService;

    /**
     * 결제 승인이 끝난 뒤 잔액을 올린다.
     * @param refId 관련 주문 id (추적용, 없으면 null)
     */
    public Result charge(Long userId, long amount, Long refId) {
        WalletService.LedgerResult r = walletService.charge(userId, amount, refId);
        return new Result(r.ledgerId(), r.balance());
    }

    /**
     * 상품/구독 지급을 위해 잔액을 내린다. 잔액이 부족하면 예외.
     * @param refId 관련 주문 id (추적용, 없으면 null)
     */
    public Result useForPurchase(Long userId, long amount, Long refId) {
        WalletService.LedgerResult r =
                walletService.use(userId, amount, WalletLedger.Type.PURCHASE, refId);
        return new Result(r.ledgerId(), r.balance());
    }

    /** 잔액을 내려 구독을 결제한다. 잔액이 부족하면 예외. */
    public Result useForSubscription(Long userId, long amount, Long refId) {
        WalletService.LedgerResult r =
                walletService.use(userId, amount, WalletLedger.Type.SUBSCRIPTION, refId);
        return new Result(r.ledgerId(), r.balance());
    }

    /** 원장 id + 처리 후 잔액 */
    public record Result(Long ledgerId, long balance) {
    }
}
