package com.canvasflow.user.controller;

import com.canvasflow.user.dto.MyPageResponse;
import com.canvasflow.user.service.MyPageService;
import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지 요약(프로필 + 게시글/팔로우/구독/알림 카운트).
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
}
