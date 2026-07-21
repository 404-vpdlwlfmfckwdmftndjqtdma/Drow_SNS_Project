package com.canvasflow.purchase;

import com.canvasflow.purchase.repository.PurchaseItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 다른 모듈이 구매 여부를 확인할 때 쓰는 공개 창구.
 *
 * 판정 기준은 purchase_items(기능별 권한) 한 곳이다.
 * 부분 구매가 가능하므로 "이 글을 샀다" = "이 글에서 뭐라도 하나 샀다" 를 뜻한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReader {

    private final PurchaseItemRepository purchaseItemRepository;

    /** 이 글에서 뭐라도 하나 구매했는지 */
    public boolean hasPurchased(Long buyerId, Long postId) {
        if (buyerId == null) {
            return false;
        }
        return purchaseItemRepository.existsByBuyerIdAndPostId(buyerId, postId);
    }

    /**
     * 이 뷰어가 이 글에서 구매한 기능 key 목록. (부분 구매 판정용)
     * capability 는 PostExtension.key() 와 매칭. 비로그인이면 빈 Set.
     */
    public Set<String> purchasedKeys(Long buyerId, Long postId) {
        if (buyerId == null) {
            return Set.of();
        }
        return Set.copyOf(purchaseItemRepository.findCapabilities(buyerId, postId));
    }

    /** 목록용: 여러 게시물 중 뭐라도 구매한 글의 postId */
    public Set<Long> findPurchasedPostIds(Long buyerId, List<Long> postIds) {
        if (buyerId == null || postIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(purchaseItemRepository.findPurchasedPostIds(buyerId, postIds));
    }
}
