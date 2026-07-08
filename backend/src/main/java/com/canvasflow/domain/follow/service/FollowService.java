package com.canvasflow.domain.follow.service;

import com.canvasflow.domain.follow.entity.Follow;
import com.canvasflow.domain.follow.repository.FollowRepository;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new CanvasflowException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new CanvasflowException(ErrorCode.ALREADY_FOLLOWING);
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));

        followRepository.save(Follow.builder().follower(follower).following(following).build());
        // TODO: NotificationService 연동 - "OO님이 나를 팔로우했습니다" 알림 저장
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.FOLLOW_NOT_FOUND));
        followRepository.delete(follow);
    }

    // TODO: getFollowingList / getFollowerList (Page<FollowUserResponse> 반환) 구현
}
