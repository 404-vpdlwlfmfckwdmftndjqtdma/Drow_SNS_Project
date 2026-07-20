package com.canvasflow.entitlement;

import com.canvasflow.post.ContentAccessPolicy;
import com.canvasflow.purchase.PurchaseReader;
import com.canvasflow.subscription.SubscriptionReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * [entitlement 도메인] post 의 ContentAccessPolicy 창구를 구현한다.
 *
 * 개별구매(purchase)와 채널구독(subscription) 정보를 취합해
 * "이 뷰어가 이 글에서 잠금 해제한 기능 key 목록"을 만들어 준다.
 *
 * 자기 테이블이 없는 순수 조율 도메인(Domain Service). 두 Reader 창구만 의존한다.
 * post·블러 모듈은 구매/구독 로직을 전혀 몰라도 되고, 결과 Set 만 보고 판단한다.
 */
@Component
@RequiredArgsConstructor
public class EntitlementPolicy implements ContentAccessPolicy {

    // 해제 가능한 전체 기능 key. 블러 모듈의 key() 와 1:1.
    private static final Set<String> ALL_KEYS = Set.of("textBlur", "imageBlur", "videoWatermark");

    private final SubscriptionReader subscriptionReader;
    private final PurchaseReader purchaseReader;

    @Override
    public Set<String> unlockedKeys(Long viewerId, Long postId, Long authorId) {
        if (viewerId == null) {
            return Set.of(); // 비로그인 → 전부 잠금
        }
        // 작성자 본인 → 전부 해제
        if (viewerId.equals(authorId)) {
            return ALL_KEYS;
        }
        // 채널(작가) 구독 중 → 전부 해제
        if (subscriptionReader.isSubscribed(viewerId, authorId)) {
            return ALL_KEYS;
        }
        // 그 외 → 이 글에서 구매한 품목(capability)만 부분 해제
        Set<String> unlocked = new HashSet<>(purchaseReader.purchasedKeys(viewerId, postId));
        unlocked.retainAll(ALL_KEYS); // 알 수 없는 key 는 무시 (방어)
        return unlocked;
    }
}
