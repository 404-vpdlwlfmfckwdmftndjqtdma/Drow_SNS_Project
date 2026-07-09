package com.canvasflow.subscription.service;

import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import com.canvasflow.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long subscribe(Long subscriberId, SubscribeRequest request) {
        subscriptionRepository.findBySubscriberIdAndTargetTypeAndTargetId(
                subscriberId, request.targetType(), request.targetId()
        ).ifPresent(existing -> {
            if (existing.getStatus() == SubscriptionStatus.ACTIVE) {
                throw new CanvasflowException(ErrorCode.ALREADY_SUBSCRIBED);
            }
        });

        if (!userRepository.existsById(subscriberId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }

        Subscription subscription = Subscription.builder()
                .subscriberId(subscriberId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .tier(request.tier())
                .build();

        // TODO: NotificationService 연동 - 대상(작가/채널 소유자)에게 "새 구독자" 알림 저장
        return subscriptionRepository.save(subscription).getId();
    }

    @Transactional
    public void unsubscribe(Long subscriberId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        // TODO: subscription.getSubscriber().getId().equals(subscriberId) 검증
        subscription.cancel();
    }
}
