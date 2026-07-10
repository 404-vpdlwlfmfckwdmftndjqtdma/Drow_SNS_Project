package com.canvasflow.comment.controller;

import com.canvasflow.comment.dto.CommentCreateRequest;
import com.canvasflow.comment.dto.CommentResponse;
import com.canvasflow.comment.dto.CommentUpdateRequest;
import com.canvasflow.comment.service.CommentService;
import com.canvasflow.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 원댓글/대댓글 작성 엔드포인트를 따로 안 나누고 CommentCreateRequest.parentId로만 구분한다.
 * 로그인 사용자 식별은 다른 도메인(Like/Follow/Notification)과 동일하게 X-User-Id 헤더로 임시 수신한다.
 * (JWT 필터/로그인은 auth 도메인 담당 - refresh_tokens 테이블 미비로 아직 로그인이 안 되는 상태라
 * comment 모듈은 auth 완성을 기다리지 않고 팀의 임시 관례를 그대로 따름. 나중에 JWT 인증이 준비되면
 * @AuthenticationPrincipal AuthMember로 다 같이 전환 예정.)
 */
@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.create(userId, postId, request)));
    }

    // 로그인 없이도 조회는 가능 (댓글 읽기는 공개 콘텐츠).
    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(postId, pageable)));
    }

    @PutMapping("/api/v1/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.update(id, userId, request)));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        commentService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
