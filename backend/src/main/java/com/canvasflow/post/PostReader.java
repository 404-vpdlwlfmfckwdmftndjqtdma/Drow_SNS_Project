package com.canvasflow.post;

import com.canvasflow.global.media.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * [post 모듈의 읽기 창구]
 * 다른 모듈이 게시글 정보를 조회할 때 쓰는 인터페이스.
 */
public interface PostReader {

    Optional<PostInfo> getPostInfo(Long postId);

    /**
     * 이 글에서 지금 살 수 있는 상품 목록 (판매자가 정한 기능별 가격).
     * 결제 화면에 "무엇을 얼마에 살 수 있는지" 띄울 때 쓴다. 없으면 빈 목록.
     */
    List<ProductInfo> getProducts(Long postId);

    /**
     * 구매 직전 가격 확인용. 판매 중지됐거나 없는 상품이면 empty.
     * 가격은 반드시 이 창구로 서버에서 조회한다(클라이언트가 보낸 금액 신뢰 금지).
     */
    Optional<ProductInfo> getProduct(Long postId, String capability);

    /** 판매 상품 한 건. capability 는 PostExtension.key() 와 1:1. */
    record ProductInfo(Long postId, Long authorId, String capability, BigDecimal price) {}

    // mypage 모듈이 마이페이지 "창작물" 통계(postCount)를 채우려고 추가함 - post 담당자 확인 부탁드립니다.
    // (구현은 PostReaderImpl.countByAuthorId 참고, PostRepository.countByUserIdAndDeletedAtIsNull 사용)
    long countByAuthorId(Long userId);

    /**
     * 마이페이지/타인 프로필의 포트폴리오 그리드(PortfolioGrid)용 게시글 목록 (최신 작성순).
     * 썸네일은 media의 첫 번째(sortOrder 기준) 항목이다.
     *
     * ★ viewerId(지금 보는 사람)를 반드시 넘길 것. 목록도 피드와 똑같이 렌더 파이프라인을 거치므로
     *   비구독자에게는 블러 처리된 content/썸네일이 나간다. 비로그인이면 null.
     *   (실제로 이 창구가 파이프라인을 건너뛰어 타인 프로필로 원문이 유출된 사고가 있었다.)
     */
    List<PostSummary> getPostsByAuthorId(Long userId, Long viewerId);

    // mypage 모듈이 마이페이지 "조회수" 통계를 채우려고 추가함 - post 담당자 확인 부탁드립니다.
    // (구현은 PostReaderImpl.sumViewCountByAuthorId 참고, PostRepository.sumViewCountByUserId 사용)
    long sumViewCountByAuthorId(Long userId);

    // mypage의 "내가 좋아요한 글 / 댓글 단 글" 목록용 창구.
    // 호출 측(mypage)이 LikeReader/CommentReader 등에서 얻은 postId 목록을 넘기면,
    // 삭제 글을 제외하고 블러 등 확장 모듈 렌더 파이프라인을 적용한 결과를 "입력 id 순서대로" 돌려준다.
    // content는 원문이 아니라 viewerId 기준 가공본이다(비구독자는 블러 치환) - 원문 유출 방지를 위해 반드시 이 창구를 쓸 것.
    // likeCount/commentCount는 post 소관이 아니므로 없다 - 호출 측이 LikeReader.summarize 등으로 채운다.
    List<PostView> getViewablePosts(List<Long> postIds, Long viewerId);

    // follow 모듈의 "팔로우한 사람들 피드"용 창구. 호출 측(follow)이 자기 리포지토리로 얻은 팔로잉 유저 id
    // 목록을 넘기면, 그 유저들이 쓴 글을 최신순으로 모아 getViewablePosts와 동일한 렌더 파이프라인(블러 등)을
    // 적용해서 돌려준다. authorIds가 비어 있으면(아무도 안 팔로우) 빈 리스트.
    List<PostView> getPostsByAuthorIds(List<Long> authorIds, Long viewerId);

    // search 모듈의 "태그 검색"용 창구 - post 담당자 확인 부탁드립니다.
    // 부분/대소문자 무관 태그 일치 검색이고, getViewablePosts와 동일한 렌더 파이프라인(블러 등)을 적용한다.
    List<PostView> searchByTag(String tag, Long viewerId);

    // 렌더 파이프라인 통과 후의 게시글 한 건 (getViewablePosts 전용 반환 타입)
    record PostView(
            Long postId,
            Long userId,
            String content,          // viewerId 기준 가공본 (비구독자는 블러 구간이 ●로 치환됨)
            List<String> tags,
            List<ViewMedia> media,
            Long viewCount,
            LocalDateTime createdAt,
            String nickname
    ) {}

    record ViewMedia(String url, MediaType mediaType) {}

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
