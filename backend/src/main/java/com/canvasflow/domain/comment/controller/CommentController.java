package com.canvasflow.domain.comment.controller;

import com.canvasflow.domain.comment.dto.CommentCreateRequest;
import com.canvasflow.domain.comment.dto.CommentResponse;
import com.canvasflow.domain.comment.service.CommentService;
import com.canvasflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Long>> create(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.create(postId, userId, request)));
    }

    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getByPost(
            @PathVariable Long postId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getByPost(postId, pageable).map(CommentResponse::from)));
    }

    @PutMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CommentCreateRequest request) {
        commentService.update(commentId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId) {
        commentService.delete(commentId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
