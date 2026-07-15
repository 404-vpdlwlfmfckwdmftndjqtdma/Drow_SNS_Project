package com.canvasflow.purchase.repository;

import com.canvasflow.purchase.entity.PostPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostPurchaseRepository extends JpaRepository<PostPurchase, Long> {

    /** 중복 구매 검증 + 상세 조회 열람 판정용 */
    boolean existsByBuyerIdAndPostId(Long buyerId, Long postId);

    /**
     * 피드 목록용: 화면에 표시할 게시물들 중 내가 구매한 것만 한 번에 조회.
     * 서비스에서 postId만 뽑아 Set으로 만들어 비교하면 N+1 방지.
     */
    List<PostPurchase> findByBuyerIdAndPostIdIn(Long buyerId, List<Long> postIds);

    /** 내 구매 내역 */
    Page<PostPurchase> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
}