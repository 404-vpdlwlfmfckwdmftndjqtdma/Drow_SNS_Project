package com.canvasflow.purchase.repository;

import com.canvasflow.purchase.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

    /** 상세 조회용: 이 뷰어가 이 글에서 산 capability 목록 */
    @Query("select pi.capability from PurchaseItem pi where pi.buyerId = :buyerId and pi.postId = :postId")
    List<String> findCapabilities(Long buyerId, Long postId);

    /**
     * 피드 목록용: 여러 글에 대해 이 뷰어가 산 (postId, capability) 조합을 한 번에 조회.
     * 서비스에서 postId별로 묶으면 N+1 방지.
     */
    @Query("select pi.postId, pi.capability from PurchaseItem pi "
            + "where pi.buyerId = :buyerId and pi.postId in :postIds")
    List<Object[]> findPostIdAndCapabilities(Long buyerId, List<Long> postIds);
}
