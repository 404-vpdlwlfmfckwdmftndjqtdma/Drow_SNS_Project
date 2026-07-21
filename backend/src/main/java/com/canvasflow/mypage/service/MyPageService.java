package com.canvasflow.mypage.service;

import com.canvasflow.follow.FollowFacade;
import com.canvasflow.mypage.MyPageFacade;
import com.canvasflow.mypage.dto.MyPagePostResponse;
import com.canvasflow.mypage.dto.MyPageResponse;
import com.canvasflow.post.PostReader;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 마이페이지 요약 정보 집계.
 * 본인 조회(GET /api/v1/mypage, X-User-Id)와 타인 조회(GET /api/v1/mypage/{userId}, 구 "채널 상세" 기능 대체)에
 * 동일한 메서드를 공용으로 쓴다.
 * user/follow/post 모듈의 구현체를 직접 참조하지 않고 각 모듈 기본 패키지의
 * UserFacade/FollowFacade/PostReader 인터페이스로만 의존한다 (반대 방향 의존은 없음 -> 순환 없음).
 * 이 클래스 자신도 mypage 모듈 기본 패키지의 MyPageFacade를 구현해서, 다른 모듈이 나중에
 * 마이페이지 요약이 필요해지면 MyPageService를 직접 참조하지 않고 이 인터페이스로만 의존할 수 있게 해둔다.
 * TODO: SubscriptionService 쪽에 카운트 조회 메서드가 추가되면 subscriptionCount도 실제 값으로 교체 (현재는 0 placeholder).
 *       followingCount / followerCount는 FollowFacade, postCount / viewCount는 PostReader로 이미 실제 값 연동됨.
 */
@RequiredArgsConstructor
@Service
public class MyPageService implements MyPageFacade {

    private final UserFacade userFacade;
    private final FollowFacade followFacade;
    private final PostReader postReader;

    @Override
    @Transactional(readOnly = true)
    public MyPageResponse getSummary(Long userId) {
        UserProfileView profile = userFacade.getProfileView(userId);

        long followingCount = followFacade.countFollowing(userId);
        long followerCount = followFacade.countFollowers(userId);
        long postCount = postReader.countByAuthorId(userId);
        long viewCount = postReader.sumViewCountByAuthorId(userId);

        // TODO: subscriptionCount는 subscription 쪽 Facade 나오면 실제 집계 쿼리로 교체
        return new MyPageResponse(
                profile.id(),
                profile.nickname(),
                profile.profileImageUrl(),
                profile.bio(),
                postCount, followingCount, followerCount, 0L, viewCount
        );
    }

    /**
     * 마이페이지/타인 프로필 포트폴리오 그리드용 게시글 목록 (최신 작성순) - PostReader.getPostsByAuthorId 그대로 매핑.
     * viewerId를 그대로 전달해서 post 쪽 렌더 파이프라인(블러 등)이 보는 사람 기준으로 콘텐츠를 가공하게 한다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MyPagePostResponse> getPosts(Long authorId, Long viewerId) {
        return postReader.getPostsByAuthorId(authorId, viewerId).stream()
                .map(post -> new MyPagePostResponse(
                        post.postId(),
                        post.content(),
                        post.thumbnailUrl(),
                        post.thumbnailUrl() != null,
                        post.isVideo()
                ))
                .toList();
    }
}
