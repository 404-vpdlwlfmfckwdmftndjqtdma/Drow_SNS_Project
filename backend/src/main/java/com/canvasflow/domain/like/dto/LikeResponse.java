package com.canvasflow.domain.like.dto;

public record LikeResponse(
        boolean liked,
        long likeCount
) {
}
