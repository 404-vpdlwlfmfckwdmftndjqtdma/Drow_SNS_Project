package com.canvasflow.domain.follow.dto;

public record FollowUserResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
}
