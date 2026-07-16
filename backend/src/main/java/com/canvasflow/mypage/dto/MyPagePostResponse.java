package com.canvasflow.mypage.dto;

/**
 * 마이페이지/타인 프로필 포트폴리오 그리드(프론트 PortfolioGrid)용 게시글 요약.
 * post 도메인에는 title이 없어서 content를 그대로 내려주고, 화면에서 제목처럼 쓸 짧은 텍스트가
 * 필요하면 프론트에서 content를 잘라 쓴다. hasMedia는 thumbnailUrl 존재 여부로 판단한다.
 */
public record MyPagePostResponse(
        Long postId,
        String content,
        String thumbnailUrl,
        boolean hasMedia,
        boolean isVideo
) {
}
