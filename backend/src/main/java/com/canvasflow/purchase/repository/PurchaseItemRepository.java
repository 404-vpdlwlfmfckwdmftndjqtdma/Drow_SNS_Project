package com.canvasflow.purchase.repository;

import com.canvasflow.purchase.entity.PurchaseItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** 중복 구매 방지: 이 글의 이 기능을 이미 샀는지 */
    boolean existsByBuyerIdAndPostIdAndCapability(Long buyerId, Long postId, String capability);

    /** 이 글에서 뭐라도 하나 샀는지 (부분 구매 포함) */
    boolean existsByBuyerIdAndPostId(Long buyerId, Long postId);

    /** 내 구매 내역 (최신순) */
    Page<PurchaseItem> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    /** 목록용: 여러 글 중 뭐라도 구매한 글의 postId */
    @Query("select distinct pi.postId from PurchaseItem pi "
            + "where pi.buyerId = :buyerId and pi.postId in :postIds")
    List<Long> findPurchasedPostIds(Long buyerId, List<Long> postIds);
}
