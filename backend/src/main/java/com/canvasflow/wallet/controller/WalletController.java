package com.canvasflow.wallet.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.wallet.WalletReader;
import com.canvasflow.wallet.dto.WalletBalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 지갑 조회 API. 충전은 order 모듈(/api/v1/orders/charge)이 담당하고,
 * 여기서는 "지금 얼마 남았는지"만 알려준다.
 */
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletReader walletReader;

    /** 내 잔액. 결제 화면에서 "보유 토큰" 표시용. 지갑이 없으면 0. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getMyBalance(
            @AuthenticationPrincipal AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        long balance = walletReader.getBalance(authMember.userId());
        return ResponseEntity.ok(ApiResponse.ok(new WalletBalanceResponse(balance)));
    }
}
