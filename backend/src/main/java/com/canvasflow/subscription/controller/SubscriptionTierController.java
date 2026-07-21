package com.canvasflow.subscription.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.subscription.dto.SubscriptionTierDtos.*;
import com.canvasflow.subscription.service.SubscriptionTierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PostMapping("/api/v1/channels/me/tiers")
    public ResponseEntity<TierResponse> create(
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody TierCreateRequest request) {
        TierResponse response = tierService.create(requireLogin(authMember), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 채널의 등급 목록 (공개 - 로그인 불필요) */
    @GetMapping("/api/v1/channels/{channelId}/tiers")
    public ResponseEntity<List<TierResponse>> getTiers(@PathVariable Long channelId) {
        return ResponseEntity.ok(tierService.getTiers(channelId));
    }

    /** 등급 수정 (내 채널의 tier만, level 변경 불가) */
    @PutMapping("/api/v1/channels/me/tiers/{tierId}")
    public ResponseEntity<TierResponse> update(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long tierId,
            @Valid @RequestBody TierUpdateRequest request) {
        return ResponseEntity.ok(tierService.update(requireLogin(authMember), tierId, request));
    }

    /** 등급 삭제 (내 채널의 tier만) */
    @DeleteMapping("/api/v1/channels/me/tiers/{tierId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long tierId) {
        tierService.delete(requireLogin(authMember), tierId);
        return ResponseEntity.noContent().build();
    }

    // /channels/me/** 는 "내 채널" 관리이므로 로그인 필수. 조회(getTiers)만 예외로 열려 있다.
    // SecurityConfig가 아직 permitAll이라 필터를 통과해도 authMember가 null로 들어올 수 있어 여기서 막는다.
    private Long requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        return authMember.userId();
    }
}
