package com.canvasflow.subscription.service;

import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.dto.SubscriptionResponse;
import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;
import com.canvasflow.subscription.entity.SubscriptionTier;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import com.canvasflow.subscription.repository.SubscriptionTierRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.wallet.WalletCharger;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTierRepository tierRepository;
    private final UserFacade userFacade;
    private final WalletCharger walletCharger;   // wallet 창구 (차감)

    /**
     * 채널 구독 신청.
     *
     * "채널"은 별도 개체가 아니라 작가(유저) 본인이다 - channelId 에는 작가의 userId 가 들어간다.
     * 구독하면 그 작가 글의 블러가 전부 해제된다(EntitlementPolicy 가 구독을 보고 ALL_KEYS 반환).
     *
     * 결제는 지갑 차감이다. 외부 결제는 order 모듈의 충전에서만 일어난다.
     */
    @Transactional
    public Long subscribe(Long subscriberId, Long channelId, SubscribeRequest request) {
        if (!userFacade.existsById(subscriberId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        // 채널 = 작가(유저)이므로 채널 존재 검증은 곧 유저 존재 검증이다
        if (!userFacade.existsById(channelId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        // 자기 자신 구독 방지 (작성자는 이미 전부 열람 가능)
        if (subscriberId.equals(channelId)) {
            throw new CanvasflowException(ErrorCode.SUBSCRIBE_SELF_CHANNEL);
        }

        SubscriptionTier tier = resolveTier(request.tierId());

        // 1) 기존 구독 조회
        Optional<Subscription> existing = subscriptionRepository
                .findBySubscriberIdAndChannelId(subscriberId, channelId);

        if (existing.isPresent() && existing.get().isBenefitActive()) {
            throw new CanvasflowException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        // 2) 유료 등급이면 지갑에서 차감 (금액은 서버의 tier 가격 - 조작 불가).
        //    잔액이 모자라면 여기서 예외 → 아무것도 저장되지 않고 화면은 충전을 유도한다.
        boolean paid = tier != null && tier.getMonthlyPrice().signum() > 0;
        if (paid) {
            walletCharger.useForSubscription(
                    subscriberId, tier.getMonthlyPrice().longValueExact(), channelId);
        }

        // 3) 저장 or 재시작
        if (existing.isPresent()) {
            Subscription sub = existing.get();
            if (paid) sub.startPaidPeriod(tier);
            else sub.reactivate(tier);  // 무료 구독 복귀
            return sub.getId();
        }
        return createSubscription(subscriberId, channelId, tier, paid);
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

    private Long createSubscription(Long subscriberId, Long channelId, SubscriptionTier tier, boolean paid) {
        Subscription subscription = Subscription.builder()
                .subscriberId(subscriberId)
                .channelId(channelId)
                .tier(tier)   // 엔티티 생성자에서 채널-등급 일치 검증됨
                .build();
        if (paid) subscription.startPaidPeriod(tier);

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
