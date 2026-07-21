package com.canvasflow.subscription.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.dto.SubscriptionResponse;
import com.canvasflow.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /** 구독 신청 */
    @PostMapping("/api/v1/channels/{channelId}/subscriptions")
    public ResponseEntity<Long> subscribe(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long channelId,
            @RequestBody SubscribeRequest request) {
        Long subscriptionId = subscriptionService.subscribe(requireLogin(authMember), channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionId);
    }

    /** 구독 해지 */
    @DeleteMapping("/api/v1/channels/{channelId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long channelId) {
        subscriptionService.unsubscribe(requireLogin(authMember), channelId);
        return ResponseEntity.noContent().build();
    }

    /** 내 구독 목록 */
    @GetMapping("/api/v1/subscriptions/me")
    public ResponseEntity<Page<SubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal AuthMember authMember,
            Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(requireLogin(authMember), pageable));
    }

    // 구독은 전부 "로그인한 본인" 기준이므로 토큰이 없으면 401.
    // SecurityConfig가 아직 permitAll이라 필터를 통과해도 authMember가 null로 들어올 수 있어 여기서 막는다.
    private Long requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        return authMember.userId();
    }
}
