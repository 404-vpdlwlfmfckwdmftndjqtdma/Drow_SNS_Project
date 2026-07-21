package com.canvasflow.subscription;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.payment.PaymentGateway;
import com.canvasflow.subscription.dto.SubscribeRequest;
import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionStatus;
import com.canvasflow.subscription.entity.SubscriptionTier;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import com.canvasflow.subscription.repository.SubscriptionTierRepository;
import com.canvasflow.subscription.service.SubscriptionService;
import com.canvasflow.user.UserFacade;
import com.canvasflow.wallet.WalletCharger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

/**
 * 구독 신청 / 재구독 / 해지 흐름 테스트.
 *
 * 등장인물:
 *  - 구독자 userId = 1
 *  - 채널 10번, 등급: 서포터(id=5, level 2)
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final Long SUBSCRIBER_ID = 1L;
    private static final Long CHANNEL_ID = 10L;
    private static final Long TIER_ID = 5L;

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Mock
    SubscriptionTierRepository tierRepository;

    @Mock
    UserFacade userFacade;

    @Mock
    WalletCharger walletCharger;

    @InjectMocks
    SubscriptionService subscriptionService;

    @Mock
    private PaymentGateway paymentGateway;

    // ===== 헬퍼 =====

    private SubscriptionTier supporterTier() {
        return SubscriptionTier.builder()
                .channelId(CHANNEL_ID)
                .name("서포터")
                .level(2)
                .monthlyPrice(BigDecimal.valueOf(5000))
                .build();
    }

    private Subscription existingSubscription(SubscriptionTier tier) {
        return Subscription.builder()
                .subscriberId(SUBSCRIBER_ID)
                .channelId(CHANNEL_ID)
                .tier(tier)
                .build();
    }

    /** 구독자와 채널 주인(작가) 둘 다 실재하는 상태. 채널 = 작가(유저)라 검증도 유저 조회다. */
    private void userExists() {
        given(userFacade.existsById(SUBSCRIBER_ID)).willReturn(true);
        given(userFacade.existsById(CHANNEL_ID)).willReturn(true);
    }

    private void noExistingSubscription() {
        given(subscriptionRepository.findBySubscriberIdAndChannelId(SUBSCRIBER_ID, CHANNEL_ID))
                .willReturn(Optional.empty());
    }

    // ===== 1. 신규 구독 =====

    @Test
    @DisplayName("유료 구독 신청: 해당 tier로 새 구독이 저장된다")
    void Pay_Subscription() {
        userExists();
        noExistingSubscription();
        SubscriptionTier tier = supporterTier();
        given(tierRepository.findByIdAndDeletedFalse(TIER_ID)).willReturn(Optional.of(tier));
        // save가 저장된 엔티티를 그대로 돌려주도록
        willAnswer(inv -> inv.getArgument(0))
                .given(subscriptionRepository).save(any(Subscription.class));

        subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(TIER_ID));

        // 저장된 Subscription의 내용까지 검증
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();
        assertThat(saved.getSubscriberId()).isEqualTo(SUBSCRIBER_ID);
        assertThat(saved.getChannelId()).isEqualTo(CHANNEL_ID);
        assertThat(saved.getTier()).isEqualTo(tier);
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        // 결제는 지갑 차감이며, 금액은 서버의 tier 가격(5,000원)이어야 한다 - 금액 조작 방어
        verify(walletCharger).useForSubscription(SUBSCRIBER_ID, 5000L, CHANNEL_ID);
    }

    @Test
    @DisplayName("무료 구독은 지갑을 차감하지 않는다")
    void Free_Subscription_No_Charge() {
        userExists();
        noExistingSubscription();
        willAnswer(inv -> inv.getArgument(0))
                .given(subscriptionRepository).save(any(Subscription.class));

        subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(null));

        verify(walletCharger, never()).useForSubscription(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("잔액 부족: 구독이 저장되지 않는다")
    void No_Money() {
        userExists();
        noExistingSubscription();
        given(tierRepository.findByIdAndDeletedFalse(TIER_ID)).willReturn(Optional.of(supporterTier()));
        willThrow(new CanvasflowException(ErrorCode.WALLET_INSUFFICIENT_BALANCE))
                .given(walletCharger).useForSubscription(anyLong(), anyLong(), anyLong());

        assertThatThrownBy(() ->
                subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(TIER_ID)))
                .isInstanceOf(CanvasflowException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("본인 채널은 구독할 수 없다")
    void Self_Subscribe_Unable() {
        given(userFacade.existsById(SUBSCRIBER_ID)).willReturn(true);

        assertThatThrownBy(() ->
                subscriptionService.subscribe(SUBSCRIBER_ID, SUBSCRIBER_ID, new SubscribeRequest(TIER_ID)))
                .isInstanceOf(CanvasflowException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("무료 구독 신청(tierId=null): tier 없이 저장되고 tier 조회는 일어나지 않는다")
    void Free_Subscription() {
        userExists();
        noExistingSubscription();
        willAnswer(inv -> inv.getArgument(0))
                .given(subscriptionRepository).save(any(Subscription.class));

        subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(null));

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getTier()).isNull();
        verify(tierRepository, never()).findByIdAndDeletedFalse(anyLong());
    }

    // ===== 2. 신청 실패 케이스 =====

    @Test
    @DisplayName("존재하지 않는 유저의 신청은 실패한다")
    void No_User() {
        given(userFacade.existsById(SUBSCRIBER_ID)).willReturn(false);

        assertThatThrownBy(() ->
                subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(TIER_ID)))
                .isInstanceOf(CanvasflowException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는(또는 삭제된) tier로는 구독할 수 없다")
    void No_Tier() {
        userExists();
        given(tierRepository.findByIdAndDeletedFalse(TIER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(TIER_ID)))
                .isInstanceOf(CanvasflowException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("혜택 기간이 남은 구독 중이면 다시 신청할 수 없다")
    void Duplicate() {
        userExists();
        SubscriptionTier tier = supporterTier();
        // 유료 구독은 status가 ACTIVE인 것만으로는 부족하고 expiresAt이 남아 있어야 혜택이 살아있다.
        // startPaidPeriod로 30일 이용권을 시작시켜야 실제 "구독 중" 상태가 된다.
        Subscription active = existingSubscription(tier);
        active.startPaidPeriod(tier);
        given(tierRepository.findByIdAndDeletedFalse(TIER_ID)).willReturn(Optional.of(tier));
        given(active.isBenefitActive()).willReturn(true);
        given(subscriptionRepository.findBySubscriberIdAndChannelId(SUBSCRIBER_ID, CHANNEL_ID))
                .willReturn(Optional.of(active));

        assertThatThrownBy(() ->
                subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(TIER_ID)))
                .isInstanceOf(CanvasflowException.class);

        // 중복 신청은 지갑도 건드리면 안 된다 (이중 결제 방지의 핵심)
        verify(walletCharger, never()).useForSubscription(anyLong(), anyLong(), anyLong());
        verify(subscriptionRepository, never()).save(any());
        verify(paymentGateway, never()).confirm(any(), any(), anyLong());
    }

    // ===== 3. 재구독 (핵심: 새 행 insert가 아니라 기존 행 재활성화) =====

    @Test
    @DisplayName("해지했던 채널을 재구독하면 기존 행이 reactivate되고 save는 호출되지 않는다")
    void Resubscribe() {
        userExists();
        SubscriptionTier oldTier = supporterTier();
        SubscriptionTier newTier = SubscriptionTier.builder()
                .channelId(CHANNEL_ID).name("VIP").level(3)
                .monthlyPrice(BigDecimal.valueOf(10000)).build();

        Subscription canceled = existingSubscription(oldTier);
        canceled.cancel();  // CANCELED 상태로 만들기
        ReflectionTestUtils.setField(canceled, "id", 99L);

        given(tierRepository.findByIdAndDeletedFalse(TIER_ID)).willReturn(Optional.of(newTier));
        given(subscriptionRepository.findBySubscriberIdAndChannelId(anyLong(), anyLong()))
                .willReturn(Optional.of(canceled));

        subscriptionService.subscribe(SUBSCRIBER_ID, CHANNEL_ID, new SubscribeRequest(TIER_ID));

        // 유니크 제약 때문에 새 행을 만들면 안 된다 - 이 검증이 이 테스트의 존재 이유
        verify(subscriptionRepository, never()).save(any());
        assertThat(canceled.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(canceled.getTier()).isEqualTo(newTier);  // 등급 갈아타기도 반영
    }

    // ===== 4. 해지 =====

    @Test
    @DisplayName("구독 해지: 상태가 CANCELED로 바뀐다")
    void Unsubscribe() {
        Subscription active = existingSubscription(supporterTier());
        given(subscriptionRepository.findBySubscriberIdAndChannelId(SUBSCRIBER_ID, CHANNEL_ID))
                .willReturn(Optional.of(active));

        subscriptionService.unsubscribe(SUBSCRIBER_ID, CHANNEL_ID);

        assertThat(active.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
    }

    @Test
    @DisplayName("구독한 적 없는 채널은 해지할 수 없다")
    void subscribefail() {
        given(subscriptionRepository.findBySubscriberIdAndChannelId(SUBSCRIBER_ID, CHANNEL_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.unsubscribe(SUBSCRIBER_ID, CHANNEL_ID))
                .isInstanceOf(CanvasflowException.class);
    }

    @Test
    @DisplayName("이미 해지한 구독은 다시 해지할 수 없다")
    void DoubleFailure() {
        Subscription canceled = existingSubscription(supporterTier());
        canceled.cancel();
        given(subscriptionRepository.findBySubscriberIdAndChannelId(SUBSCRIBER_ID, CHANNEL_ID))
                .willReturn(Optional.of(canceled));

        assertThatThrownBy(() -> subscriptionService.unsubscribe(SUBSCRIBER_ID, CHANNEL_ID))
                .isInstanceOf(CanvasflowException.class);
    }
}