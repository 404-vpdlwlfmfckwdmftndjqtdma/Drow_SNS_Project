package com.canvasflow.user.dto;

public record MyPageResponse(
        UserResponse profile,
        long postCount,
        long followingCount,
        long followerCount,
        long subscriptionCount,
        long unreadNotificationCount
) {
}
