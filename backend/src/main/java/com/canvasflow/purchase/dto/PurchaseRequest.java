package com.canvasflow.purchase.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 단건 구매 요청.
 * 금액은 받지 않는다 - 서버가 가격표(post_products)에서 조회한다(조작 차단).
 * 결제 수단도 받지 않는다 - 모든 구매는 지갑 잔액에서 차감된다(충전은 order 모듈 담당).
 */
public record PurchaseRequest(
        @NotBlank String capability   // 구매할 기능 key ("textBlur", "imageBlur" ...)
) {
}
