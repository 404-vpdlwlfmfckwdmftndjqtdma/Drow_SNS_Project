package com.canvasflow.domain.post.repository;

import com.canvasflow.domain.post.dto.PostSearchCondition;
import com.canvasflow.domain.post.dto.PostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 검색/필터/정렬(콘텐츠 타입, 채널, 태그, 키워드, 정렬기준)이 들어가는 동적 쿼리.
 * TODO: QueryDSL 도입 후 PostRepositoryImpl 에서 구현.
 */
public interface PostRepositoryCustom {

    Page<PostSummaryResponse> search(PostSearchCondition condition, Pageable pageable);
}
