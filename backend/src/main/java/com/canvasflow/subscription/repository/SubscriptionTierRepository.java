package com.canvasflow.subscription.repository;

import com.canvasflow.subscription.entity.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionTierRepository extends JpaRepository<SubscriptionTier, Long> {

    /** 채널의 구독 상품 목록 (삭제 제외, 저렴한 순) */
    List<SubscriptionTier> findByChannelIdAndDeletedFalseOrderByMonthlyPriceAsc(Long channelId);

    Optional<SubscriptionTier> findByIdAndDeletedFalse(Long id);

    /** 같은 채널에 같은 이름의 상품이 이미 있는지 (수정 시 자기 자신은 제외) */
    boolean existsByChannelIdAndNameAndDeletedFalse(Long channelId, String name);

    boolean existsByChannelIdAndNameAndDeletedFalseAndIdNot(Long channelId, String name, Long id);


    Optional<SubscriptionTier> findByIdAndChannelIdAndDeletedFalse(Long id, Long channelId);
}
