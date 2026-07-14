package com.canvasflow.mypage.controller;

import com.canvasflow.mypage.dto.MyPageResponse;
import com.canvasflow.mypage.service.MyPageService;
import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지 요약(프로필 + 게시글/팔로우/구독 카운트).
 * GET (X-User-Id, 본인) / GET /{userId} (타인 조회 - 구 "채널 상세" 기능을 대체함, 인증 불필요) 둘 다 지원한다.
 * 상세 목록은 각 도메인 컨트롤러(PostController, FollowController, SubscriptionController,
 * NotificationController)의 "내 것 조회" 엔드포인트를 사용한다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPageResponse>> getSummary(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(myPageService.getSummary(userId)));
    }

    /** 타인의 마이페이지 조회 - 구 "채널 상세" 기능 대체. 로그인 여부와 무관하게 누구나 조회 가능. */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<MyPageResponse>> getSummaryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(myPageService.getSummary(userId)));
    }
}
