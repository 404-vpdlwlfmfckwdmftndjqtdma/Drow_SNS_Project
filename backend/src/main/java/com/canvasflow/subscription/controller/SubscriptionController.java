package com.canvasflow.subscription.controller;

import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.service.SubscriptionService;
import com.canvasflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> subscribe(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.subscribe(userId, request)));
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long subscriptionId) {
        subscriptionService.unsubscribe(userId, subscriptionId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // TODO: GET /me - 내가 구독 중인 채널/작가 목록 (마이페이지용)
}
