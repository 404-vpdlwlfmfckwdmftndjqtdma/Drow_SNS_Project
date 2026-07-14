package com.canvasflow.subscription.repository;

import com.canvasflow.subscription.entity.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionTierRepository extends JpaRepository<SubscriptionTier, Long> {

    /** 채널의 등급 목록 (삭제 제외, 레벨 오름차순) */
    List<SubscriptionTier> findByChannelIdAndDeletedFalseOrderByLevelAsc(Long channelId);

    Optional<SubscriptionTier> findByIdAndDeletedFalse(Long id);

    boolean existsByChannelIdAndLevelAndDeletedFalse(Long channelId, int level);

    long countByChannelIdAndDeletedFalse(Long channelId);

    Optional<SubscriptionTier> findByIdAndChannelIdAndDeletedFalse(Long id, Long channelId);
}
