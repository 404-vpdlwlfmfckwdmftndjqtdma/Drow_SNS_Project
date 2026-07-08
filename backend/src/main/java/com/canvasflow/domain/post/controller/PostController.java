package com.canvasflow.domain.post.controller;

import com.canvasflow.domain.post.dto.*;
import com.canvasflow.domain.post.service.PostService;
import com.canvasflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(postService.create(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostSummaryResponse>>> search(
            PostSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(postService.search(condition, pageable)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getDetail(
            @PathVariable Long postId,
            @RequestHeader(value = "X-User-Id", required = false) Long viewerId) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getDetail(postId, viewerId)));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PostUpdateRequest request) {
        postService.update(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        postService.delete(postId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
