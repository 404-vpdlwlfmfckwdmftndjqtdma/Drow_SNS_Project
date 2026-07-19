package com.canvasflow.order.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.order.dto.ChargeOrderRequest;
import com.canvasflow.order.dto.OrderConfirmRequest;
import com.canvasflow.order.dto.OrderConfirmResponse;
import com.canvasflow.order.dto.OrderCreateResponse;
import com.canvasflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 API.
 *
 * 결제 흐름:
 *   ① POST /api/v1/orders/charge   → 서버가 orderId·금액 확정
 *   ② (프론트) 그 orderId 로 토스 결제창 → 리다이렉트 복귀
 *   ③ POST /api/v1/orders/{orderId}/confirm → 승인 + 지갑 반영
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 충전 주문 생성 (결제 전) */
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createChargeOrder(
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody ChargeOrderRequest request) {

        Long userId = requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.createChargeOrder(userId, request.amount())));
    }

    /** 결제 확정 (결제 후). 금액은 서버가 주문에서 꺼내 쓰므로 받지 않는다. */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderConfirmResponse>> confirm(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable String orderId,
            @RequestBody OrderConfirmRequest request) {

        Long userId = requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.confirm(userId, orderId, request.paymentKey())));
    }

    private Long requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        return authMember.userId();
    }
}
