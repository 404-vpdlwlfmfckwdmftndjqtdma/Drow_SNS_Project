package com.canvasflow.domain.subscription.service;

import com.canvasflow.domain.subscription.dto.SubscribeRequest;
import com.canvasflow.domain.subscription.entity.Subscription;
import com.canvasflow.domain.subscription.entity.SubscriptionStatus;
import com.canvasflow.domain.subscription.repository.SubscriptionRepository;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
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

        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription = Subscription.builder()
                .subscriber(subscriber)
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
