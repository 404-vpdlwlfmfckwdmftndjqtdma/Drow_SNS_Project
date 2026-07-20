package com.canvasflow.order.entity;

/**
 * 주문의 목적. 결제가 끝난 뒤 무엇을 지급할지 결정하는 기준.
 *
 * 프론트가 결제 후에 알려주는 게 아니라, 주문을 만들 때 서버가 기록해 둔다.
 * (프론트 말을 믿으면 "1000원 결제하고 구독 달라" 같은 조작이 가능해진다.)
 */
public enum OrderPurpose {

    /** 지갑 충전. 결제 금액이 그대로 잔액이 된다. */
    CHARGE,

    /** 게시물 단건 구매. (지갑에서 차감) */
    PURCHASE,

    /** 구독. (지갑에서 차감) */
    SUBSCRIPTION
}
