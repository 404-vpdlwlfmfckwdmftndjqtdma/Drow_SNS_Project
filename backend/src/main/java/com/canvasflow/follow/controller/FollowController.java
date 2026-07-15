package com.canvasflow.follow.controller;

import com.canvasflow.follow.service.FollowService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 다른 도메인 컨트롤러(UserController)와 동일하게 X-User-Id 헤더 대신
 * JwtAuthenticationFilter가 채워주는 @AuthenticationPrincipal AuthMember를 사용한다
 * (프론트가 Authorization: Bearer 헤더만 보내고 X-User-Id는 별도로 보내지 않기 때문).
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long targetUserId) {
        requireLogin(authMember);
        followService.follow(authMember.userId(), targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long targetUserId) {
        requireLogin(authMember);
        followService.unfollow(authMember.userId(), targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 로그인 없이도 다른 사람 마이페이지는 볼 수 있으므로, 이 엔드포인트는 로그인을 강제하지 않는다.
    // 비로그인이면 항상 false(팔로우 안 한 상태)로 응답한다.
    @GetMapping("/{targetUserId}/status")
    public ResponseEntity<ApiResponse<Boolean>> getFollowStatus(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long targetUserId) {
        boolean following = authMember != null && followService.isFollowing(authMember.userId(), targetUserId);
        return ResponseEntity.ok(ApiResponse.ok(following));
    }

    // TODO: GET /me/following, GET /me/followers 엔드포인트 추가

    private void requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
    }
}
