package com.canvasflow.subscription.service;

import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.dto.SubscriptionResponse;
import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;
import com.canvasflow.subscription.entity.SubscriptionTier;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import com.canvasflow.subscription.repository.SubscriptionTierRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTierRepository tierRepository;
    private final UserFacade userFacade;
    // TODO: ChannelRepository 추가 (채널 존재 검증용)

    @Transactional
    public Long subscribe(Long subscriberId, Long channelId, SubscribeRequest request) {
        if (!userFacade.existsById(subscriberId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        // TODO: 채널 존재 검증
        // if (!channelRepository.existsById(channelId)) {
        //     throw new CanvasflowException(ErrorCode.CHANNEL_NOT_FOUND);
        // }

        SubscriptionTier tier = resolveTier(request.tierId());

        return subscriptionRepository
                .findBySubscriberIdAndChannelId(subscriberId, channelId)
                .map(existing -> reactivateOrThrow(existing, tier))
                .orElseGet(() -> createSubscription(subscriberId, channelId, tier));
    }

    /** tierId가 null이면 무료 구독, 있으면 존재하는 등급인지 확인 */
    private SubscriptionTier resolveTier(Long tierId) {
        if (tierId == null) return null;
        return tierRepository.findByIdAndDeletedFalse(tierId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.TIER_NOT_FOUND));
    }

    private Long reactivateOrThrow(Subscription existing, SubscriptionTier tier) {
        if (existing.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new CanvasflowException(ErrorCode.ALREADY_SUBSCRIBED);
        }
        existing.reactivate(tier);
        return existing.getId();
    }

    private Long createSubscription(Long subscriberId, Long channelId, SubscriptionTier tier) {
        Subscription subscription = Subscription.builder()
                .subscriberId(subscriberId)
                .channelId(channelId)
                .tier(tier)   // 엔티티 생성자에서 채널-등급 일치 검증됨
                .build();

        // TODO: NotificationService 연동 - 채널 주인에게 "새 구독자" 알림
        return subscriptionRepository.save(subscription).getId();
    }

    @Transactional
    public void unsubscribe(Long subscriberId, Long channelId) {
        Subscription subscription = subscriptionRepository
                .findBySubscriberIdAndChannelId(subscriberId, channelId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new CanvasflowException(ErrorCode.SUBSCRIPTION_NOT_FOUND); // 이미 해지된 상태
        }

        subscription.cancel();
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getMySubscriptions(Long subscriberId, Pageable pageable) {
        return subscriptionRepository
                .findWithTierBySubscriberIdAndStatus(subscriberId, SubscriptionStatus.ACTIVE, pageable)
                .map(SubscriptionResponse::from);
    }
}
