package com.canvasflow.follow;

import com.canvasflow.follow.entity.Follow;
import com.canvasflow.follow.repository.FollowRepository;
import com.canvasflow.post.FollowingPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/*
    피드에서 내가 팔로우 한 사람들의 글이 먼저 보이도록 하기 위한 코드
 */
@Component
@RequiredArgsConstructor
public class FollowingPolicyImpl implements FollowingPolicy {

    private final FollowRepository followRepository;

    @Override
    public Set<Long> followingIds(Long viewerId){
        if(viewerId == null){
            return Set.of();
        }
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(viewerId).stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());
    }
}
