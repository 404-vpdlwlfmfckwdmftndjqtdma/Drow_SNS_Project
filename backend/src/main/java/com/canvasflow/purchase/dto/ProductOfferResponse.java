package com.canvasflow.purchase.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 결제 화면 데이터.
 * 화면은 balance(보유 토큰)와 각 상품 price(결제할 금액)를 비교해
 * 부족분이 있으면 충전을 먼저 유도한다.
 */
public record ProductOfferResponse(
        Long postId,
        long balance,          // 내 지갑 잔액(원)
        List<Offer> offers     // 이 글에서 살 수 있는 것들
) {
    /** purchased=true 면 이미 구매해 잠금이 풀린 상품 */
    public record Offer(String capability, BigDecimal price, boolean purchased) {}
}
