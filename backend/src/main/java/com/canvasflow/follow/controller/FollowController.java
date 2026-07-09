package com.canvasflow.follow.controller;

import com.canvasflow.follow.service.FollowService;
import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long targetUserId) {
        followService.follow(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long targetUserId) {
        followService.unfollow(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // TODO: GET /me/following, GET /me/followers 엔드포인트 추가
}
