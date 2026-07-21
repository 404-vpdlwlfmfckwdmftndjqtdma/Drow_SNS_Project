package com.canvasflow.subscription;

import com.canvasflow.purchase.PurchaseReader;
import com.canvasflow.subscription.entity.Subscription;
import com.canvasflow.subscription.entity.SubscriptionTier;
import com.canvasflow.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 등급별 콘텐츠 노출 시나리오 테스트.
 *
 * 등장인물:
 *  - 채널 10번 (주인 userId = 100)
 *  - 등급: 팬(level 1), 서포터(level 2)
 *  - 게시물 77번: requiredLevel = 2 (서포터 이상 공개)
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTests {

    private static final Long CHANNEL_ID = 10L;
    private static final Long AUTHOR_ID = 100L;
    private static final Long POST_ID = 77L;
    private static final int REQUIRED_LEVEL = 2;

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Mock
    PurchaseReader purchaseReader;

    @InjectMocks
    ContentAccessService contentAccessService;

    // ===== 테스트 데이터 헬퍼 =====

    private SubscriptionTier tier(int level) {
        return SubscriptionTier.builder()
                .channelId(CHANNEL_ID)
                .name("등급" + level)
                .level(level)
                .monthlyPrice(BigDecimal.valueOf(level * 1000L))
                .build();
    }

    /** viewerId가 CHANNEL_ID 채널을 해당 tier로 구독 중인 상태를 만들어 mock에 등록 */
    private Subscription activeSubscription(Long viewerId, SubscriptionTier tier) {
        Subscription sub = Subscription.builder()
                .subscriberId(viewerId)
                .channelId(CHANNEL_ID)
                .tier(tier)
                .build();
        given(subscriptionRepository.findBySubscriberIdAndChannelId(viewerId, CHANNEL_ID))
                .willReturn(Optional.of(sub));
        return sub;
    }

    private void noSubscription(Long viewerId) {
        given(subscriptionRepository.findBySubscriberIdAndChannelId(viewerId, CHANNEL_ID))
                .willReturn(Optional.empty());
    }

    private boolean canView(Long viewerId) {
        return contentAccessService.canView(
                viewerId, CHANNEL_ID, POST_ID, AUTHOR_ID, REQUIRED_LEVEL);
    }

    // ===== 1. 구독/구매와 무관하게 결정되는 케이스 =====

    @Test
    @DisplayName("전체 공개 글(requiredLevel=0)은 비로그인도 볼 수 있다")
    void anyone_reads() {
        boolean result = contentAccessService.canView(null, CHANNEL_ID, POST_ID, AUTHOR_ID, 0);

        assertThat(result).isTrue();
        // 판정이 첫 조건에서 끝나므로 DB 조회조차 없어야 한다
        verify(subscriptionRepository, never()).findBySubscriberIdAndChannelId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("비로그인 유저는 잠긴 글을 볼 수 없다")
    void non_login_blocks_the_lock() {
        assertThat(canView(null)).isFalse();
    }

    @Test
    @DisplayName("작성자 본인은 구독 없이도 자기 글을 볼 수 있다")
    void You_read_allTheTime() {
        assertThat(canView(AUTHOR_ID)).isTrue();
        verify(subscriptionRepository, never()).findBySubscriberIdAndChannelId(anyLong(), anyLong());
    }

    // ===== 2. 구독 등급 판정 =====

    @Test
    @DisplayName("요구 레벨과 같은 등급 구독자는 볼 수 있다 (경계값)")
    void same_level_subscriber_reads() {
        Long viewer = 1L;
        activeSubscription(viewer, tier(2));   // 서포터(2) 구독, 요구 레벨도 2

        assertThat(canView(viewer)).isTrue();
        // 구독으로 통과했으므로 구매 조회까지 가지 않아야 한다
        verify(purchaseReader, never()).hasPurchased(anyLong(), anyLong());
    }

    @Test
    @DisplayName("요구 레벨보다 높은 등급 구독자는 볼 수 있다")
    void top_level_subscriber_views() {
        Long viewer = 2L;
        activeSubscription(viewer, tier(3));   // VIP(3) > 요구(2)
        assertThat(canView(viewer)).isTrue();
    }

    @Test
    @DisplayName("요구 레벨보다 낮은 등급 구독자는 볼 수 없다 (구매도 없음)")
    void lower_level_subscriber_blocked() {
        Long viewer = 3L;
        activeSubscription(viewer, tier(1));   // 팬(1) < 요구(2)
        given(purchaseReader.hasPurchased(viewer, POST_ID)).willReturn(false);

        assertThat(canView(viewer)).isFalse();
    }

    @Test
    @DisplayName("무료 구독자(tier=null)는 잠긴 글을 볼 수 없다")
    void free_Subscribers_Block() {
        Long viewer = 4L;
        activeSubscription(viewer, null);      // 등급 없는 팔로우
        given(purchaseReader.hasPurchased(viewer, POST_ID)).willReturn(false);

        assertThat(canView(viewer)).isFalse();
    }

    @Test
    @DisplayName("해지한 구독자는 상위 등급이었어도 볼 수 없다")
    void terminator_Blocking() {
        Long viewer = 5L;
        Subscription sub = activeSubscription(viewer, tier(3));
        sub.cancel();                          // VIP였지만 해지
        given(purchaseReader.hasPurchased(viewer, POST_ID)).willReturn(false);

        assertThat(canView(viewer)).isFalse();
    }

    // ===== 3. 단건 구매 판정 =====

    @Test
    @DisplayName("구독이 없어도 단건 구매자는 볼 수 있다")
    void Buyer_views() {
        Long viewer = 6L;
        noSubscription(viewer);
        given(purchaseReader.hasPurchased(viewer, POST_ID)).willReturn(true);

        assertThat(canView(viewer)).isTrue();
    }

    @Test
    @DisplayName("등급 미달 구독자여도 단건 구매했으면 볼 수 있다")
    void Ifpurchasedread() {
        Long viewer = 7L;
        activeSubscription(viewer, tier(1));   // 팬(1) < 요구(2)
        given(purchaseReader.hasPurchased(viewer, POST_ID)).willReturn(true);

        assertThat(canView(viewer)).isTrue();
    }

    @Test
    @DisplayName("구독도 구매도 없으면 볼 수 없다")
    void nothingblockit() {
        Long viewer = 8L;
        noSubscription(viewer);
        given(purchaseReader.hasPurchased(viewer, POST_ID)).willReturn(false);

        assertThat(canView(viewer)).isFalse();
    }

    // ===== 4. effectiveLevel (피드 목록용) =====

    @Test
    @DisplayName("effectiveLevel: 비로그인은 0")
    void levelunlogin() {
        assertThat(contentAccessService.effectiveLevel(null, CHANNEL_ID)).isZero();
    }

    @Test
    @DisplayName("effectiveLevel: 비구독자는 0")
    void levelunSubscribers() {
        noSubscription(9L);
        assertThat(contentAccessService.effectiveLevel(9L, CHANNEL_ID)).isZero();
    }

    @Test
    @DisplayName("effectiveLevel: ACTIVE 구독자는 tier의 level")
    void levelSubscribers() {
        activeSubscription(11L, tier(2));
        assertThat(contentAccessService.effectiveLevel(11L, CHANNEL_ID)).isEqualTo(2);
    }
}


