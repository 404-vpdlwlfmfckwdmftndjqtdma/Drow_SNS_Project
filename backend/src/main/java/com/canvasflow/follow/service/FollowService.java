package com.canvasflow.follow.service;

import com.canvasflow.follow.FollowFacade;
import com.canvasflow.follow.dto.FollowUserResponse;
import com.canvasflow.follow.entity.Follow;
import com.canvasflow.follow.repository.FollowRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 내가 팔로우하고 있는 사람 목록 (채널 "전체 보기" 화면 + 우측 채널 미리보기 패널 공용).
     * 최근에 팔로우한 사람이 먼저 나오도록 정렬한다 - 미리보기 패널은 이 순서 그대로 앞에서 N개만 잘라서 쓰면 된다.
     * 목록 규모가 크지 않은 지금 단계에서는 follow 건마다 UserFacade를 호출하는 방식으로 두고,
     * 나중에 목록이 커지면 UserFacade에 id 목록 기반 벌크 조회를 추가해 N+1을 없애면 된다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowingList(Long userId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> {
                    UserProfileView profile = userFacade.getProfileView(follow.getFollowingId());
                    return new FollowUserResponse(profile.id(), profile.nickname(), profile.profileImageUrl(), profile.bio());
                })
                .toList();
    }

    /**
     * 이 userId를 팔로우하고 있는 사람 목록 (팔로워 목록 화면용, 본인/타인 공용).
     * getFollowingList와 대칭 구조 - followerId/followingId만 반대로 조회한다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowerList(Long userId) {
        return followRepository.findByFollowingIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> {
                    UserProfileView profile = userFacade.getProfileView(follow.getFollowerId());
                    return new FollowUserResponse(profile.id(), profile.nickname(), profile.profileImageUrl(), profile.bio());
                })
                .toList();
    }
}
