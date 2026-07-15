package com.canvasflow.like.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.like.LikeTargetType;
import com.canvasflow.like.dto.LikeResponse;
import com.canvasflow.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<LikeResponse>> like(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable LikeTargetType targetType,
            @PathVariable Long targetId) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(likeService.like(authMember.userId(), targetType, targetId)));
    }

    @DeleteMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable LikeTargetType targetType,
            @PathVariable Long targetId) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(likeService.unlike(authMember.userId(), targetType, targetId)));
    }

    // 로그인 사용자는 likedByMe + likeCount, 비로그인 사용자는 likedByMe=false + likeCount를 조회한다.
    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<LikeResponse>> getStatus(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable LikeTargetType targetType,
            @PathVariable Long targetId) {
        Long userId = authMember != null ? authMember.userId() : null;
        return ResponseEntity.ok(ApiResponse.ok(likeService.getStatus(userId, targetType, targetId)));
    }

    // 브라우저 EventSource는 커스텀 헤더를 못 보내고, 개수 자체는 누구나 봐도 되는 정보라 인증 없이 연다.
    @GetMapping(value = "/{targetType}/{targetId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable LikeTargetType targetType, @PathVariable Long targetId) {
        return likeService.subscribe(targetType, targetId);
    }

    private void requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
    }
}
