package com.canvasflow.like;

import com.canvasflow.like.entity.Like;
import com.canvasflow.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 다른 모듈이 좋아요 개수/여부를 읽기 전용으로 조회할 때 쓰는 공개 창구. */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class LikeReader {

    private final LikeRepository likeRepository;

    public long countByTarget(LikeTargetType targetType, Long targetId) {
        return likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    public boolean isLikedByUser(Long userId, LikeTargetType targetType, Long targetId) {
        return likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    // targetId별 좋아요 개수 + "이 유저가 눌렀는지"를 한 번에 배치 조회 (N+1 방지)
    public LikeSummary summarize(LikeTargetType targetType, List<Long> targetIds, Long viewerId) {
        if (targetIds.isEmpty()) {
            return new LikeSummary(Map.of(), Set.of());
        }
        List<Like> likes = likeRepository.findByTargetTypeAndTargetIdIn(targetType, targetIds);
        Map<Long, Long> counts = likes.stream()
                .collect(Collectors.groupingBy(Like::getTargetId, Collectors.counting()));
        Set<Long> likedByViewer = viewerId == null
                ? Set.of()
                : likes.stream()
                        .filter(like -> like.getUserId().equals(viewerId))
                        .map(Like::getTargetId)
                        .collect(Collectors.toSet());
        return new LikeSummary(counts, likedByViewer);
    }

    public record LikeSummary(Map<Long, Long> countsByTargetId, Set<Long> likedTargetIds) {
        public long countOf(Long targetId) {
            return countsByTargetId.getOrDefault(targetId, 0L);
        }

        public boolean likedByViewer(Long targetId) {
            return likedTargetIds.contains(targetId);
        }
    }
}
