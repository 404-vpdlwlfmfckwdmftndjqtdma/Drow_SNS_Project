package com.canvasflow.follow.service;

import com.canvasflow.follow.FollowFacade;
import com.canvasflow.follow.entity.Follow;
import com.canvasflow.follow.repository.FollowRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 담당: 팔로우/언팔로우.
 * user 모듈의 구현체(UserService)를 직접 참조하지 않고 user 모듈 기본 패키지의 UserFacade
 * 인터페이스로만 의존한다.
 * 이 클래스 자신도 follow 모듈 기본 패키지의 FollowFacade를 구현해서, 다른 모듈이 나중에 팔로우
 * 기능이 필요해지면 FollowService를 직접 참조하지 않고 이 인터페이스로만 의존할 수 있게 해둔다.
 */
@RequiredArgsConstructor
@Service
public class FollowService implements FollowFacade {

    private final FollowRepository followRepository;
    private final UserFacade userFacade;

    @Override
    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new CanvasflowException(ErrorCode.ALREADY_FOLLOWING);
        }
        if (!userFacade.existsById(followerId) || !userFacade.existsById(followingId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }

        followRepository.save(Follow.builder().followerId(followerId).followingId(followingId).build());
        // TODO: NotificationService 연동 - "OO님이 나를 팔로우했습니다" 알림 저장
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.FOLLOW_NOT_FOUND));
        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /** 내가 팔로우하고 있는 사람 수 (마이페이지 "팔로잉" 카운트). */
    @Override
    @Transactional(readOnly = true)
    public long countFollowing(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    /** 나를 팔로우하는 사람 수 (마이페이지 "팔로워" 카운트). */
    @Override
    @Transactional(readOnly = true)
    public long countFollowers(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    // TODO: getFollowingList / getFollowerList (Page<FollowUserResponse> 반환) 구현
}
