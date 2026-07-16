package com.canvasflow.subscription;


import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionReader {

    private final SubscriptionRepository subscriptionRepository;

    public boolean isSubscribed(Long viewerId, Long authorId) {
        if(viewerId == null)
            return false;
        return subscriptionRepository
                .findBySubscriberIdAndChannelId(viewerId, authorId)
                .map(Subscription::effectiveLevel)
                .orElse(0) > 0;
    }
}
