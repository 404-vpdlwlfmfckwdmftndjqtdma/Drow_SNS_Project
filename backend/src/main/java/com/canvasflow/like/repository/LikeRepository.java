package com.canvasflow.like.repository;

import com.canvasflow.like.entity.Like;
import com.canvasflow.like.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, LikeTargetType targetType, Long targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, LikeTargetType targetType, Long targetId);

    long countByTargetTypeAndTargetId(LikeTargetType targetType, Long targetId);

    // 댓글 목록 조회 시 대상 여러 개의 좋아요를 한 번에 가져와 개수/여부를 집계하기 위함 (N+1 방지)
    List<Like> findByTargetTypeAndTargetIdIn(LikeTargetType targetType, List<Long> targetIds);
}
