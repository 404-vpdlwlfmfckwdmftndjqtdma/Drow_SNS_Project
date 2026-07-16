package com.canvasflow.follow.repository;

import com.canvasflow.follow.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    // 채널 목록(전체 보기)/우측 채널 미리보기 패널 둘 다 최신 팔로우 순으로 보여주기 위한 정렬 조회.
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);
}
