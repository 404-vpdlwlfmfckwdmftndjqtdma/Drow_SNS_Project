package com.canvasflow.post.repository;

import com.canvasflow.post.entity.PostProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostProductRepository extends JpaRepository<PostProduct, Long> {

    /** 이 글에서 지금 살 수 있는 상품들 (판매 중지된 건 제외) */
    List<PostProduct> findByPostIdAndOnSaleTrue(Long postId);

    /** 구매 시 가격 확인용. 판매 중지된 상품은 살 수 없다. */
    Optional<PostProduct> findByPostIdAndCapabilityAndOnSaleTrue(Long postId, String capability);

    /** 글 수정 시 가격표 전체 교체용 (판매 중지분까지 포함해 조회) */
    List<PostProduct> findByPostId(Long postId);

    void deleteAllByPostId(Long postId);
}
