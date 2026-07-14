package com.canvasflow.subscription.controller;

import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.dto.SubscriptionResponse;
import com.canvasflow.subscription.dto.SubscriptionTierDtos.*;
import com.canvasflow.subscription.service.SubscriptionService;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.subscription.service.SubscriptionTierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 구독 등급 API.
 * 관리(생성/수정/삭제)는 /channels/me 아래 - 항상 로그인 유저 본인 채널 대상.
 * 조회는 /channels/{channelId} - 누구나 다른 채널의 등급 목록을 볼 수 있음.
 */
@RestController
@RequiredArgsConstructor
public class SubscriptionTierController {

    private final SubscriptionTierService tierService;

    /** 등급 생성 (내 채널에) */
    @PostMapping("/api/channels/me/tiers")
    public ResponseEntity<TierResponse> create(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            @Valid @RequestBody TierCreateRequest request) {
        Long loginUserId = 1L; // TODO: user.getId()
        TierResponse response = tierService.create(loginUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 채널의 등급 목록 (공개) */
    @GetMapping("/api/channels/{channelId}/tiers")
    public ResponseEntity<List<TierResponse>> getTiers(@PathVariable Long channelId) {
        return ResponseEntity.ok(tierService.getTiers(channelId));
    }

    /** 등급 수정 (내 채널의 tier만, level 변경 불가) */
    @PutMapping("/api/channels/me/tiers/{tierId}")
    public ResponseEntity<TierResponse> update(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            @PathVariable Long tierId,
            @Valid @RequestBody TierUpdateRequest request) {
        Long loginUserId = 1L; // TODO
        return ResponseEntity.ok(tierService.update(loginUserId, tierId, request));
    }

    /** 등급 삭제 (내 채널의 tier만) */
    @DeleteMapping("/api/channels/me/tiers/{tierId}")
    public ResponseEntity<Void> delete(
            /* @AuthenticationPrincipal CustomUserDetails user, */
            @PathVariable Long tierId) {
        Long loginUserId = 1L; // TODO
        tierService.delete(loginUserId, tierId);
        return ResponseEntity.noContent().build();
    }
}