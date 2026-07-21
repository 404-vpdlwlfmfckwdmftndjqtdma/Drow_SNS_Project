package com.canvasflow.entitlement;

import com.canvasflow.purchase.PurchaseReader;
import com.canvasflow.subscription.SubscriptionReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 콘텐츠 잠금 해제 판정 테스트 - 블러가 풀리는지를 결정하는 최종 지점.
 *
 * 등장인물:
 *  - 작성자 userId = 100 (채널 = 작가 본인이므로 channelId 도 100)
 *  - 게시물 77번 (textBlur / imageBlur 판매 중)
 *
 * 판정 순서: 비로그인 → 작성자 본인 → 구독자 → 개별 구매
 * 앞 조건에서 끝나면 뒤 조회는 하지 않아야 한다(불필요한 쿼리 방지).
 */
@ExtendWith(MockitoExtension.class)
class EntitlementPolicyTest {

    private static final Long AUTHOR_ID = 100L;
    private static final Long POST_ID = 77L;
    private static final Set<String> ALL_KEYS = Set.of("textBlur", "imageBlur", "videoWatermark");

    @Mock
    SubscriptionReader subscriptionReader;

    @Mock
    PurchaseReader purchaseReader;

    @InjectMocks
    EntitlementPolicy policy;

    private Set<String> unlockedFor(Long viewerId) {
        return policy.unlockedKeys(viewerId, POST_ID, AUTHOR_ID);
    }

    // ===== 1. 구독/구매를 보기 전에 끝나는 케이스 =====

    @Test
    @DisplayName("비로그인은 아무것도 해제되지 않는다 (전부 블러)")
    void 비로그인() {
        assertThat(unlockedFor(null)).isEmpty();

        // 판정이 첫 조건에서 끝나므로 조회조차 없어야 한다
        verify(subscriptionReader, never()).isSubscribed(anyLong(), anyLong());
        verify(purchaseReader, never()).purchasedKeys(anyLong(), anyLong());
    }

    @Test
    @DisplayName("작성자 본인은 구독/구매 없이 전부 해제된다")
    void 작성자_본인() {
        assertThat(unlockedFor(AUTHOR_ID)).isEqualTo(ALL_KEYS);

        verify(subscriptionReader, never()).isSubscribed(anyLong(), anyLong());
        verify(purchaseReader, never()).purchasedKeys(anyLong(), anyLong());
    }

    // ===== 2. 구독 판정 (채널 구매) =====

    @Test
    @DisplayName("구독자는 텍스트·이미지 블러가 한 번에 전부 풀린다")
    void 구독자는_전부_해제() {
        Long viewer = 1L;
        given(subscriptionReader.isSubscribed(viewer, AUTHOR_ID)).willReturn(true);

        assertThat(unlockedFor(viewer)).isEqualTo(ALL_KEYS);

        // 구독으로 통과했으므로 구매 조회까지 가지 않아야 한다
        verify(purchaseReader, never()).purchasedKeys(anyLong(), anyLong());
    }

    // ===== 3. 개별 구매 판정 (부분 해제) =====

    @Test
    @DisplayName("구독하지 않았다면 산 기능만 풀린다")
    void 개별_구매는_부분_해제() {
        Long viewer = 2L;
        given(subscriptionReader.isSubscribed(viewer, AUTHOR_ID)).willReturn(false);
        given(purchaseReader.purchasedKeys(viewer, POST_ID)).willReturn(Set.of("imageBlur"));

        assertThat(unlockedFor(viewer)).containsExactly("imageBlur");
    }

    @Test
    @DisplayName("구독도 구매도 없으면 아무것도 풀리지 않는다")
    void 아무것도_없으면_전부_블러() {
        Long viewer = 3L;
        given(subscriptionReader.isSubscribed(viewer, AUTHOR_ID)).willReturn(false);
        given(purchaseReader.purchasedKeys(viewer, POST_ID)).willReturn(Set.of());

        assertThat(unlockedFor(viewer)).isEmpty();
    }

    @Test
    @DisplayName("알 수 없는 capability는 무시된다 (오타·구버전 데이터 방어)")
    void 미인정_key는_무시() {
        Long viewer = 4L;
        given(subscriptionReader.isSubscribed(viewer, AUTHOR_ID)).willReturn(false);
        given(purchaseReader.purchasedKeys(viewer, POST_ID))
                .willReturn(Set.of("textBlur", "imageBlurr", "sound"));

        assertThat(unlockedFor(viewer)).containsExactly("textBlur");
    }
}
