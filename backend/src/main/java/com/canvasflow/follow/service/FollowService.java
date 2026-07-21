package com.canvasflow.follow.service;

import com.canvasflow.follow.FollowFacade;
import com.canvasflow.follow.dto.FollowUserResponse;
import com.canvasflow.follow.entity.Follow;
import com.canvasflow.follow.repository.FollowRepository;
import com.canvasflow.notification.NotificationFacade;
import com.canvasflow.notification.NotificationTargetType;
import com.canvasflow.notification.NotificationType;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    private final NotificationFacade notificationFacade;

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
        notifyNewFollower(followerId, followingId);
    }

    // 알림 저장 실패가 팔로우 자체를 막으면 안 되는 부가 효과라 예외를 삼킨다 (comment/like 모듈과 동일한 패턴).
    private void notifyNewFollower(Long followerId, Long followingId) {
        try {
            String followerNickname = userFacade.getNicknameOrThrow(followerId);
            notificationFacade.notify(
                    followingId, followerId, NotificationType.NEW_FOLLOWER,
                    NotificationTargetType.USER, followerId,
                    followerNickname + "님이 회원님을 팔로우하기 시작했습니다.");
        } catch (Exception e) {
            // 알림 저장 실패는 무시 - 다음 접속 시 팔로워 목록으로 확인 가능
        }
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
     * UserFacade.getProfileViews로 대상 유저들의 프로필을 한 번에 조회해서 N+1을 피한다.
     * 조회 도중 탈퇴 등으로 없어진 유저는 결과 목록에서 그냥 빠진다(예외 없음).
     */
    @Override
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowingList(Long userId) {
        List<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId);
        Map<Long, UserProfileView> profilesById = userFacade.getProfileViews(
                follows.stream().map(Follow::getFollowingId).toList()
        );

        return follows.stream()
                .map(follow -> profilesById.get(follow.getFollowingId()))
                .filter(profile -> profile != null)
                .map(profile -> new FollowUserResponse(profile.id(), profile.nickname(), profile.profileImageUrl(), profile.bio()))
                .toList();
    }

    /**
     * 이 userId를 팔로우하고 있는 사람 목록 (팔로워 목록 화면용, 본인/타인 공용).
     * getFollowingList와 대칭 구조 - followerId/followingId만 반대로 조회하고, 마찬가지로
     * UserFacade.getProfileViews 벌크 조회로 N+1을 피한다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowerList(Long userId) {
        List<Follow> follows = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId);
        Map<Long, UserProfileView> profilesById = userFacade.getProfileViews(
                follows.stream().map(Follow::getFollowerId).toList()
        );

        return follows.stream()
                .map(follow -> profilesById.get(follow.getFollowerId()))
                .filter(profile -> profile != null)
                .map(profile -> new FollowUserResponse(profile.id(), profile.nickname(), profile.profileImageUrl(), profile.bio()))
                .toList();
    }
}
