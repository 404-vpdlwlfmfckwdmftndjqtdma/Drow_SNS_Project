package com.canvasflow.domain.subscription.repository;

import com.canvasflow.domain.subscription.entity.Subscription;
import com.canvasflow.domain.subscription.entity.SubscriptionStatus;
import com.canvasflow.domain.subscription.entity.SubscriptionTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findBySubscriberIdAndTargetTypeAndTargetId(
            Long subscriberId, SubscriptionTargetType targetType, Long targetId);

    boolean existsBySubscriberIdAndTargetTypeAndTargetIdAndStatus(
            Long subscriberId, SubscriptionTargetType targetType, Long targetId,
            SubscriptionStatus status);

    Page<Subscription> findBySubscriberId(Long subscriberId, Pageable pageable);
}
