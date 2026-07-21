package com.canvasflow.mypage.controller;

import com.canvasflow.mypage.dto.MyPagePostResponse;
import com.canvasflow.mypage.dto.MyPageResponse;
import com.canvasflow.mypage.service.MyPageService;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 마이페이지 요약(프로필 + 게시글/팔로우/구독 카운트).
 * GET (본인) / GET /{userId} (타인 조회 - 구 "채널 상세" 기능을 대체함, 인증 불필요) 둘 다 지원한다.
 * 본인 조회는 다른 도메인 컨트롤러(UserController, FollowController)와 동일하게 X-User-Id 헤더 대신
 * JwtAuthenticationFilter가 채워주는 @AuthenticationPrincipal AuthMember를 사용한다
 * (프론트가 Authorization: Bearer 헤더만 보내고 X-User-Id는 별도로 보내지 않기 때문).
 * 상세 목록은 각 도메인 컨트롤러(PostController, FollowController, SubscriptionController,
 * NotificationController)의 "내 것 조회" 엔드포인트를 사용한다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPageResponse>> getSummary(@AuthenticationPrincipal AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(myPageService.getSummary(authMember.userId())));
    }

    /** 타인의 마이페이지 조회 - 구 "채널 상세" 기능 대체. 로그인 여부와 무관하게 누구나 조회 가능. */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<MyPageResponse>> getSummaryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(myPageService.getSummary(userId)));
    }

    /** 내 포트폴리오 그리드용 게시글 목록 (프론트 PortfolioGrid). 본인 조회라 viewerId도 자기 자신. */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<MyPagePostResponse>>> getMyPosts(@AuthenticationPrincipal AuthMember authMember) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(myPageService.getPosts(authMember.userId(), authMember.userId())));
    }

    /**
     * 타인 프로필 포트폴리오 그리드용 게시글 목록. 로그인 여부와 무관하게 누구나 조회 가능하되,
     * 로그인했으면 그 사람의 viewerId를 넘겨서 post 쪽 렌더 파이프라인(구독/구매 기준 블러 해제)이
     * 정확히 적용되도록 한다. 비로그인이면 viewerId는 null(post 쪽에서 전부 잠금 처리).
     */
    @GetMapping("/{userId}/posts")
    public ResponseEntity<ApiResponse<List<MyPagePostResponse>>> getPostsByUserId(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long userId) {
        Long viewerId = authMember != null ? authMember.userId() : null;
        return ResponseEntity.ok(ApiResponse.ok(myPageService.getPosts(userId, viewerId)));
    }
}
