package com.canvasflow.like.dto;

public record LikeResponse(
        boolean liked,
        long likeCount
) {
}
