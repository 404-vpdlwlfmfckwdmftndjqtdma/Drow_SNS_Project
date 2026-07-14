package com.canvasflow.follow;

/**
 * follow 모듈이 다른 모듈에 노출하는 기능을 모은 파사드 인터페이스.
 * com.canvasflow.follow는 이 모듈의 기본 패키지라 Spring Modulith가 자동으로 노출해준다.
 * 현재는 FollowController(같은 모듈)만 쓰지만, 다른 모듈이 나중에 팔로우 기능이 필요해지면
 * FollowService를 직접 참조하지 않고 이 인터페이스로만 의존하도록 미리 준비해둔다.
 */
public interface FollowFacade {

    void follow(Long followerId, Long followingId);

    void unfollow(Long followerId, Long followingId);
}
