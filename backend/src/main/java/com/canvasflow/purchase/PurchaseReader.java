package com.canvasflow.purchase;

import com.canvasflow.purchase.entity.PostPurchase;
import com.canvasflow.purchase.repository.PostPurchaseRepository;
import com.canvasflow.purchase.repository.PurchaseItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 다른 모듈이 구매 여부를 확인할 때 쓰는 공개 창구 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReader {

    private final PostPurchaseRepository postPurchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    public boolean hasPurchased(Long buyerId, Long postId) {
        return postPurchaseRepository.existsByBuyerIdAndPostId(
                buyerId, postId);
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

    /** 목록용: 여러 게시물 중 구매한 것만 postId Set으로 */
    public Set<Long> findPurchasedPostIds(Long buyerId, List<Long> postIds) {
        return postPurchaseRepository
                .findByBuyerIdAndPostIdIn(buyerId, postIds)
                .stream()
                .map(PostPurchase::getPostId)
                .collect(Collectors.toSet());
    }
}
