package com.canvasflow.domain.subscription.service;

import com.canvasflow.domain.subscription.entity.SubscriptionStatus;
import com.canvasflow.domain.subscription.entity.SubscriptionTargetType;
import com.canvasflow.domain.subscription.repository.SubscriptionRepository;
import com.canvasflow.global.common.ContentVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 상세 조회 시 "구독 여부에 따라 콘텐츠를 다르게 표시"하는 핵심 판정 로직.
 * PostService.getDetail() 에서 이 서비스를 호출해 locked 여부를 받아
 * PostResponse.from(post, locked) 로 마스킹 처리한다.
 *
 * TODO: BLUR/BLACKBOX/PARTIAL 각 정책별 프론트 표현은 프론트에서 locked+visibility 조합으로 렌더링.
 */
@RequiredArgsConstructor
@Service
public class ContentAccessService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public boolean isSubscribedToUser(Long subscriberId, Long userId) {
        if (subscriberId == null || userId == null) {
            return false;
        }
        return subscriptionRepository.existsBySubscriberIdAndTargetTypeAndTargetIdAndStatus(
                subscriberId, SubscriptionTargetType.USER, userId, SubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public boolean isLocked(ContentVisibility visibility, Long authorId, Long channelId, Long viewerId) {
        if (visibility == ContentVisibility.PUBLIC) {
            return false;
        }
        if (viewerId == null) {
            return true;
        }
        if (viewerId.equals(authorId)) {
            return false; // 작성자 본인은 항상 열람 가능
        }

        boolean subscribedToAuthor = isSubscribedToUser(viewerId, authorId);

        boolean subscribedToChannel = channelId != null && subscriptionRepository
                .existsBySubscriberIdAndTargetTypeAndTargetIdAndStatus(
                        viewerId, SubscriptionTargetType.CHANNEL, channelId, SubscriptionStatus.ACTIVE);

        return !(subscribedToAuthor || subscribedToChannel);
    }
}
