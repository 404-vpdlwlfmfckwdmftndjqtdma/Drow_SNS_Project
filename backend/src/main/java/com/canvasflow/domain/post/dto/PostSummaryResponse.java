package com.canvasflow.domain.post.dto;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String thumbnailUrl,
        Long authorId,
        String authorNickname,
        long viewCount,
        long likeCount,
        long commentCount,
        boolean locked,
        LocalDateTime createdAt
) {
}
