package com.canvasflow.subscription.controller;

import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.dto.SubscriptionResponse;
import com.canvasflow.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /** 구독 신청 */
    @PostMapping("/api/channels/{channelId}/subscriptions")
    public ResponseEntity<Long> subscribe(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            @PathVariable Long channelId,
            @RequestBody SubscribeRequest request) {
        Long loginUserId = 1L; // TODO: user.getId()
        Long subscriptionId = subscriptionService.subscribe(loginUserId, channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionId);
    }

    /** 구독 해지 */
    @DeleteMapping("/api/channels/{channelId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            @PathVariable Long channelId) {
        Long loginUserId = 1L; // TODO
        subscriptionService.unsubscribe(loginUserId, channelId);
        return ResponseEntity.noContent().build();
    }

    /** 내 구독 목록 */
    @GetMapping("/api/subscriptions/me")
    public ResponseEntity<Page<SubscriptionResponse>> getMySubscriptions(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            Pageable pageable) {
        Long loginUserId = 1L; // TODO
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(loginUserId, pageable));
    }
}