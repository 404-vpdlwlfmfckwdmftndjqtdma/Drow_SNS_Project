package com.canvasflow.like.controller;

import com.canvasflow.like.dto.LikeResponse;
import com.canvasflow.like.LikeTargetType;
import com.canvasflow.like.service.LikeService;
import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<LikeResponse>> like(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable LikeTargetType targetType,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(ApiResponse.ok(likeService.like(userId, targetType, targetId)));
    }

    @DeleteMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable LikeTargetType targetType,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(ApiResponse.ok(likeService.unlike(userId, targetType, targetId)));
    }

    // 로그인 없이도 개수 조회는 가능하도록 X-User-Id를 optional로 받는다 (없으면 liked=false).
    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<LikeResponse>> getStatus(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable LikeTargetType targetType,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(ApiResponse.ok(likeService.getStatus(userId, targetType, targetId)));
    }

    // 이 대상(게시글/댓글)을 보고 있는 모든 클라이언트에게 개수 변경을 실시간으로 브로드캐스트한다.
    // 브라우저 EventSource는 커스텀 헤더를 못 보내고, 개수 자체는 누구나 봐도 되는 정보라 인증 없이 연다.
    @GetMapping(value = "/{targetType}/{targetId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable LikeTargetType targetType, @PathVariable Long targetId) {
        return likeService.subscribe(targetType, targetId);
    }
}
