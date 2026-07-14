package com.canvasflow.mypage.dto;

/**
 * 마이페이지/채널(타인 조회) 공용 요약 응답.
 * user 모듈의 UserResponse(email 포함)를 그대로 쓰지 않고 필요한 프로필 필드만 펼쳐 담는다 -
 * email처럼 공개되면 안 되는 값이 섞이는 걸 원천적으로 막기 위함이다.
 * 안읽은 알림 수도 포함하지 않는다 - 그건 본인만 봐야 하는 값이라 상단바 알림 배지 전용 엔드포인트로 분리한다.
 */
public record MyPageResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String bio,
        long postCount,
        long followingCount,
        long followerCount,
        long subscriptionCount
) {
}
