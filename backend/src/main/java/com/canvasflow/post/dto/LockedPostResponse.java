package com.canvasflow.post.dto;

import java.math.BigDecimal;

/**
 * 전체 잠금 글 응답.
 * 본문/미디어/원본 URL 필드가 아예 없다 - null로 주는 게 아니라 존재하지 않아야
 * 응답을 뜯어봐도 콘텐츠가 유출되지 않는다.
 */
public record LockedPostResponse(
        Long postId,
        boolean locked,
        String thumbnailUrl,
        Long channelId,
        String channelName,
        int requiredLevel,
        String requiredTierName,
        BigDecimal singlePurchasePrice
) implements PostDetailResponse {
}
