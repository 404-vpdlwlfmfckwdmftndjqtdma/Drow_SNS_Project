package com.canvasflow.user;

/**
 * 다른 모듈(mypage 등)에 프로필을 보여줄 때 쓰는 읽기 전용 뷰.
 * email은 의도적으로 포함하지 않는다 - 이 값이 쓰이는 GET /api/v1/mypage/{userId} 같은 공개 조회
 * 엔드포인트에서 email이 노출되면 안 되기 때문이다.
 * UserFacade와 같은 위치(모듈 기본 패키지)에 둬서 별도 노출 설정 없이 다른 모듈이 쓸 수 있다.
 */
public record UserProfileView(
        Long id,
        String nickname,
        String profileImageUrl,
        String bio
) {
}
