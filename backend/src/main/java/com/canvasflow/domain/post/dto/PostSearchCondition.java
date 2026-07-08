package com.canvasflow.domain.post.dto;

/**
 * 검색/필터/정렬 조건.
 * contentType: IMAGE / TEXT / VIDEO / MIXED (null 이면 전체)
 * sort: LATEST(기본) / LIKES / COMMENTS / VIEWS
 */
public record PostSearchCondition(
        String keyword,
        Long channelId,
        String contentType,
        String tag,
        String sort
) {
}
