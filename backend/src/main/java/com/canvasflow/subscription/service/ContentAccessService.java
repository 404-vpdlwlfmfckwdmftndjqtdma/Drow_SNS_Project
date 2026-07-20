package com.canvasflow.subscription.service;

import com.canvasflow.purchase.PurchaseReader;
import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionTier;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import com.canvasflow.subscription.repository.SubscriptionTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 열람 권한 판정: 다음 중 하나면 열람 가능
 *   1. 전체 공개 글 (requiredLevel = 0)
 *   2. 본인 글
 *   3. 채널 구독 등급이 요구 레벨 이상
 *   4. 해당 게시물을 단건 구매함
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentAccessService {

    private final SubscriptionRepository subscriptionRepository;
    private final PurchaseReader purchaseReader;
    private final SubscriptionTierRepository tierRepository;

    public boolean canView(Long viewerId, Long channelId, Long postId,
                           Long authorId, int requiredLevel) {
        if (requiredLevel <= 0) return true;               // 전체 공개
        if (viewerId == null) return false;                // 비로그인
        if (viewerId.equals(authorId)) return true;        // 본인 글

        // 1) 채널 구독 등급 판정
        int level = subscriptionRepository
                .findBySubscriberIdAndChannelId(viewerId, channelId)
                .map(Subscription::effectiveLevel)
                .orElse(0);
        if (level >= requiredLevel) return true;

        // 2) 단건 구매 판정
        return purchaseReader.hasPurchased(viewerId, postId);
    }

    /**
     * 피드 목록용: 채널 등급을 한 번만 구해서 재사용.
     * 목록에서는 구매 여부까지 글마다 조회하면 N+1이 되므로,
     * viewer가 구매한 postId 목록을 IN 쿼리로 한 번에 가져와 Set으로 비교할 것.
     */
    public int effectiveLevel(Long viewerId, Long channelId) {
        if (viewerId == null) return 0;
        return subscriptionRepository
                .findBySubscriberIdAndChannelId(viewerId, channelId)
                .map(Subscription::effectiveLevel)
                .orElse(0);
    }

    /** 해당 채널에서 이 글을 열람할 수 있는 최저 등급의 이름 (구독 유도 문구용) */
    public Optional<String> minUnlockTierName(Long channelId, int requiredLevel) {
        return tierRepository.findByChannelIdAndDeletedFalseOrderByLevelAsc(channelId)
                .stream()
                .filter(t -> t.getLevel() >= requiredLevel)
                .findFirst()
                .map(SubscriptionTier::getName);
    }

}
