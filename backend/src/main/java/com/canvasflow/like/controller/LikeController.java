package com.canvasflow.like.controller;

import com.canvasflow.like.dto.LikeResponse;
import com.canvasflow.like.entity.LikeTargetType;
import com.canvasflow.like.service.LikeService;
import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
