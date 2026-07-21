package com.canvasflow.like.repository;

import com.canvasflow.like.entity.Like;
import com.canvasflow.like.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, LikeTargetType targetType, Long targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, LikeTargetType targetType, Long targetId);

    long countByTargetTypeAndTargetId(LikeTargetType targetType, Long targetId);

    // 댓글 목록 조회 시 대상 여러 개의 좋아요를 한 번에 가져와 개수/여부를 집계하기 위함 (N+1 방지)
    List<Like> findByTargetTypeAndTargetIdIn(LikeTargetType targetType, List<Long> targetIds);

    // 마이페이지 "내가 좋아요한 글" 목록용: 이 유저가 좋아요를 누른 targetId를 최근에 누른 순으로 반환한다.
    // 파생 쿼리 이름으로 단일 필드 프로젝션(findTargetIdBy...)을 시도하면 Hibernate가 엔티티 전체를 조회해버려서
    // List<Long> 반환 타입과 안 맞아 ConversionFailedException이 나므로, 명시적 JPQL로 targetId만 선택한다.
    @Query("SELECT l.targetId FROM Like l WHERE l.userId = :userId AND l.targetType = :targetType ORDER BY l.createdAt DESC")
    List<Long> findLikedTargetIds(@Param("userId") Long userId, @Param("targetType") LikeTargetType targetType);
}
