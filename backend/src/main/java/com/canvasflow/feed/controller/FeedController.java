package com.canvasflow.feed.controller;

import com.canvasflow.feed.service.FeedService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.post.PostReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 마이페이지 "내 활동" 피드 3종 엔드포인트. URL은 프론트 호환을 위해 기존 그대로(각각 follows/likes/comments
 * 하위 경로)를 쓰지만, 구현은 follow/like/comment 모듈의 컨트롤러/서비스가 아니라 이 feed 모듈이 소유한다
 * (각 담당자 코드 변경 없이 추가).
 */
@RequiredArgsConstructor
@RestController
public class FeedController {

    private final FeedService feedService;

    // 마이페이지 "팔로우" 탭.
    @GetMapping("/api/v1/follows/feed")
    public ResponseEntity<ApiResponse<List<PostReader.PostView>>> getFollowingFeed(
            @AuthenticationPrincipal AuthMember authMember) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(feedService.getFollowingFeed(authMember.userId())));
    }

    // 마이페이지 "좋아요" 탭.
    @GetMapping("/api/v1/likes/me/posts")
    public ResponseEntity<ApiResponse<List<PostReader.PostView>>> getMyLikedPosts(
            @AuthenticationPrincipal AuthMember authMember) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(feedService.getMyLikedPosts(authMember.userId())));
    }

    // 마이페이지 "댓글" 탭.
    @GetMapping("/api/v1/comments/me/posts")
    public ResponseEntity<ApiResponse<List<PostReader.PostView>>> getMyCommentedPosts(
            @AuthenticationPrincipal AuthMember authMember) {
        requireLogin(authMember);
        return ResponseEntity.ok(ApiResponse.ok(feedService.getMyCommentedPosts(authMember.userId())));
    }

    private void requireLogin(AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
    }
}
