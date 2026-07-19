package com.canvasflow.wallet;

import com.canvasflow.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * [wallet 모듈의 공개 창구 - 조회]
 * 다른 모듈(purchase/subscription 등)이 잔액을 확인할 때 쓴다.
 * 차감/충전 같은 상태 변경은 노출하지 않는다. (WalletService 내부 전용)
 */
@Service
@RequiredArgsConstructor
public class WalletReader {

    private final WalletService walletService;

    /** 이 사용자의 현재 잔액(원). 지갑이 없으면 0. */
    public long getBalance(Long userId) {
        return walletService.getBalance(userId);
    }
}
