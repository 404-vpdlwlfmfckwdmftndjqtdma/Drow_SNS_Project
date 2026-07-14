package com.canvasflow.purchase.repository;

import com.canvasflow.purchase.entity.PostPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostPurchaseRepository extends JpaRepository<PostPurchase, Long> {

    /** 상세 조회 열람 판정용 */
    boolean existsByBuyerIdAndPostIdAndStatus(Long buyerId, Long postId, PostPurchase.Status status);

    Optional<PostPurchase> findByBuyerIdAndPostId(Long buyerId, Long postId);

    /**
     * 피드 목록용: 화면에 표시할 게시물들 중 내가 구매한 것만 한 번에 조회.
     * 서비스에서 postId만 뽑아 Set으로 만들어 비교하면 N+1 방지.
     */
    List<PostPurchase> findByBuyerIdAndPostIdInAndStatus(
            Long buyerId, List<Long> postIds, PostPurchase.Status status);

    /** 내 구매 내역 */
    List<PostPurchase> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
}
