package com.canvasflow.subscription.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 채널(작가) 구독 요청.
 * 금액·결제수단은 받지 않는다 - 서버가 tier 가격을 조회해 지갑에서 차감한다.
 */
public record SubscribeRequest(
        @NotNull Long tierId
) {
}
