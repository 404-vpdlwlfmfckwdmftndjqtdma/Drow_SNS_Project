package com.canvasflow.domain.search.controller;

import com.canvasflow.domain.post.dto.PostSearchCondition;
import com.canvasflow.domain.post.dto.PostSummaryResponse;
import com.canvasflow.domain.post.service.PostService;
import com.canvasflow.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 콘텐츠 타입/채널/태그 필터 + 제목·내용 키워드 검색 + 정렬(최신순/좋아요순/댓글순/조회순)
 * 진입점. 실제 동적 쿼리는 PostRepositoryCustom(도메인 post) 에서 처리하며,
 * 이 컨트롤러는 검색 전용 URL(/api/v1/search)을 제공하기 위한 얇은 래퍼다.
 * (PostController#search 의 GET /api/v1/posts 와 동일 파라미터를 공유한다.)
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostSummaryResponse>>> search(
            PostSearchCondition condition, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(postService.search(condition, pageable)));
    }
}
