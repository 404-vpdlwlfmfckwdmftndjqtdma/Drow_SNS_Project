package com.canvasflow.follow;

import com.canvasflow.follow.dto.FollowUserResponse;

import java.util.List;

/**
 * follow 모듈이 다른 모듈에 노출하는 기능을 모은 파사드 인터페이스.
 * com.canvasflow.follow는 이 모듈의 기본 패키지라 Spring Modulith가 자동으로 노출해준다.
 * 현재는 FollowController(같은 모듈)만 쓰지만, 다른 모듈이 나중에 팔로우 기능이 필요해지면
 * FollowService를 직접 참조하지 않고 이 인터페이스로만 의존하도록 미리 준비해둔다.
 */
public interface FollowFacade {

    void follow(Long followerId, Long followingId);

    void unfollow(Long followerId, Long followingId);

    boolean isFollowing(Long followerId, Long followingId);

    long countFollowing(Long userId);

    long countFollowers(Long userId);

    /** 내가 팔로우하고 있는 사람 목록 (채널 "전체 보기" 화면용). */
    List<FollowUserResponse> getFollowingList(Long userId);

    // feed 모듈의 "팔로우한 사람들 피드"용으로 추가함 - follow 담당자 확인 부탁드립니다.
    // FollowUserResponse(follow.dto, internal)를 다른 모듈에 그대로 노출하면 Spring Modulith 경계
    // 위반이라, id만 필요한 호출부를 위해 순수 id 목록만 반환하는 창구를 따로 둔다.
    List<Long> getFollowingIds(Long userId);

    /** 이 userId를 팔로우하고 있는 사람 목록 (팔로워 목록 화면용, 본인/타인 공용). */
    List<FollowUserResponse> getFollowerList(Long userId);
}
