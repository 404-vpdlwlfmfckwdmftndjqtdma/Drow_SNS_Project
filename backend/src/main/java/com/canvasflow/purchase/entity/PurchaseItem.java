package com.canvasflow.purchase.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구매 품목. "이 뷰어가 이 글에서 무슨 기능(capability)의 잠금을 샀는가"를 한 줄씩 기록한다.
 *
 * capability 는 PostExtension.key() 와 1:1 매칭되는 문자열("textBlur", "imageBlur" ...).
 * enum/컬럼이 아니라 별도 테이블 row 로 두어, 판매 종류가 늘어도 스키마 변경 없이 확장된다.
 *
 * 결제 트랜잭션 자체(PostPurchase)와 분리된 "권한 부여" 테이블이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "purchase_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_buyer_post_capability",
                columnNames = {"buyer_id", "post_id", "capability"}), // 같은 품목 중복 부여 방지
        indexes = @Index(name = "idx_pi_buyer_post", columnList = "buyer_id, post_id")
)
public class PurchaseItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    /** 잠금 해제한 기능 key (PostExtension.key() 와 매칭). 예: "textBlur" */
    @Column(nullable = false, length = 50)
    private String capability;

    public PurchaseItem(Long buyerId, Long postId, String capability) {
        this.buyerId = buyerId;
        this.postId = postId;
        this.capability = capability;
    }
}
