package com.canvasflow.comment.controller;

import com.canvasflow.comment.dto.CommentCreateRequest;
import com.canvasflow.comment.dto.CommentResponse;
import com.canvasflow.comment.dto.CommentUpdateRequest;
import com.canvasflow.comment.dto.CommentCountResponse;
import com.canvasflow.comment.service.CommentService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 원댓글/대댓글 작성 엔드포인트를 따로 안 나누고 CommentCreateRequest.parentId로만 구분한다.
 * 로그인 사용자 식별은 JwtAuthenticationFilter가 주입한 @AuthenticationPrincipal AuthMember를 사용한다.
 */
@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(commentService.create(authMember.userId(), postId, request)));
    }

    // 로그인 없이도 조회는 가능 (댓글 읽기는 공개 콘텐츠) - 이 경우 likedByMe는 항상 false로 내려간다.
    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId, Pageable pageable) {
        Long viewerId = authMember == null ? null : authMember.userId();
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(postId, viewerId, pageable)));
    }

    @GetMapping("/api/v1/posts/{postId}/comments/count")
    public ResponseEntity<ApiResponse<CommentCountResponse>> getCommentCount(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getCommentCount(postId)));
    }

    // 이 게시글의 댓글을 보고 있는 모든 클라이언트에게 생성/수정/삭제를 실시간으로 브로드캐스트한다.
    // 브라우저 EventSource는 커스텀 헤더를 못 보내고, 댓글 읽기는 공개 콘텐츠라 인증 없이 연다.
    @GetMapping(value = "/api/v1/posts/{postId}/comments/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long postId) {
        return commentService.subscribe(postId);
    }

    @PutMapping("/api/v1/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest request) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(commentService.update(id, authMember.userId(), request)));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long id) {
        requireLogin(authMember);
        commentService.delete(id, authMember.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private void requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
    }
}
