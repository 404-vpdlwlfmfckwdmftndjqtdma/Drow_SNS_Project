package com.canvasflow.post;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * [post 모듈의 읽기 창구]
 * 다른 모듈이 게시글 정보를 조회할 때 쓰는 인터페이스.
 */
public interface PostReader {

    Optional<PostPurchaseInfo> getPurchaseInfo(Long postId);

    Optional<PostInfo> getPostInfo(Long postId);

    // mypage 모듈이 마이페이지 "창작물" 통계(postCount)를 채우려고 추가함 - post 담당자 확인 부탁드립니다.
    // (구현은 PostReaderImpl.countByAuthorId 참고, PostRepository.countByUserIdAndDeletedAtIsNull 사용)
    long countByAuthorId(Long userId);

    List<PostSearchView> findByTag(String tag);

    // mypage 모듈이 마이페이지/타인 프로필의 포트폴리오 그리드(PortfolioGrid)를 실제 게시글로 채우려고 추가함
    // - post 담당자 확인 부탁드립니다. 최신 작성순으로 반환하고, 썸네일은 media의 첫 번째(sortOrder 기준) 항목이다.
    // (구현은 PostReaderImpl.getPostsByAuthorId 참고, PostRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc 사용)
    List<PostSummary> getPostsByAuthorId(Long userId);

    // mypage 모듈이 마이페이지 "조회수" 통계를 채우려고 추가함 - post 담당자 확인 부탁드립니다.
    // (구현은 PostReaderImpl.sumViewCountByAuthorId 참고, PostRepository.sumViewCountByUserId 사용)
    long sumViewCountByAuthorId(Long userId);

    record PostPurchaseInfo(Long authorId, BigDecimal singlePurchasePrice) {}

    record PostInfo(Long authorId) {}

    // 프론트 PortfolioGrid(components/post/PortfolioGrid.tsx)의 PortfolioPost와 1:1로 맞춘 요약 정보.
    // title 대신 content를 그대로 준다 - post 도메인에는 별도 title 필드가 없다(글 하나가 최대 800자 짧은 게시글 형태).
    // 화면에서 제목처럼 보여줄 짧은 텍스트가 필요하면 호출하는 쪽(mypage)에서 content를 잘라서 쓰면 된다.
    record PostSummary(
            Long postId,
            String content,
            String thumbnailUrl,
            boolean isVideo,
            LocalDateTime createdAt
    ) {}
}
