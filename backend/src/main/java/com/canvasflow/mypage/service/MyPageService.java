package com.canvasflow.mypage.service;

import com.canvasflow.mypage.MyPageFacade;
import com.canvasflow.mypage.dto.MyPageResponse;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 요약 정보 집계.
 * 본인 조회(GET /api/v1/mypage, X-User-Id)와 타인 조회(GET /api/v1/mypage/{userId}, 구 "채널 상세" 기능 대체)에
 * 동일한 메서드를 공용으로 쓴다.
 * user 모듈의 구현체(UserService)를 직접 참조하지 않고 user 모듈 기본 패키지의 UserFacade
 * 인터페이스로만 의존한다 (user -> mypage 역방향 의존은 없음 -> 순환 없음).
 * 이 클래스 자신도 mypage 모듈 기본 패키지의 MyPageFacade를 구현해서, 다른 모듈이 나중에
 * 마이페이지 요약이 필요해지면 MyPageService를 직접 참조하지 않고 이 인터페이스로만 의존할 수 있게 해둔다.
 * TODO: PostService / FollowService / SubscriptionService 쪽에 카운트 조회 메서드가 추가되면
 *       postCount / followingCount / followerCount / subscriptionCount 실제 값으로 교체 (현재는 0 placeholder).
 */
@RequiredArgsConstructor
@Service
public class MyPageService implements MyPageFacade {

    private final UserFacade userFacade;

    @Override
    @Transactional(readOnly = true)
    public MyPageResponse getSummary(Long userId) {
        UserProfileView profile = userFacade.getProfileView(userId);

        // TODO: 실제 집계 쿼리로 교체
        return new MyPageResponse(
                profile.id(),
                profile.nickname(),
                profile.profileImageUrl(),
                profile.bio(),
                0L, 0L, 0L, 0L
        );
    }
}
