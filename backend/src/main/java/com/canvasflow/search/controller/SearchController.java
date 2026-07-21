package com.canvasflow.search.controller;

import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.post.PostReader;
import com.canvasflow.search.service.SearchService;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 검색 페이지 "태그 검색" / "유저 검색" 엔드포인트. URL은 프론트 호환을 위해 기존 그대로
 * /api/v1/posts/search, /api/v1/users/search를 쓰지만, 구현은 post/user 모듈의 컨트롤러/서비스가
 * 아니라 이 search 모듈이 소유한다 (post/user 담당자 코드 변경 없이 추가).
 */
@RequiredArgsConstructor
@RestController
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/v1/posts/search")
    public ResponseEntity<ApiResponse<List<PostReader.PostView>>> searchPostsByTag(
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam String tag) {
        Long viewerId = authMember != null ? authMember.userId() : null;
        return ResponseEntity.ok(ApiResponse.ok(searchService.searchPostsByTag(tag, viewerId)));
    }

    // 로그인 여부와 무관하게 누구나 검색 가능 (게시글 피드처럼 공개 정보).
    @GetMapping("/api/v1/users/search")
    public ResponseEntity<ApiResponse<List<UserProfileView>>> searchUsersByNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.searchUsersByNickname(nickname)));
    }
}
