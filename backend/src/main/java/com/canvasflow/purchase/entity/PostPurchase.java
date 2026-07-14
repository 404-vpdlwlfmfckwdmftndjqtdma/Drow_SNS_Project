package com.canvasflow.purchase.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 게시물 단건 구매.
 * 구독 등급이 안 돼도 이 게시물 하나만 결제해서 열람할 수 있게 하는 기능.
 * (Post 쪽에는 singlePurchasePrice 컬럼을 두고, null 이면 단건 구매 불가로 처리)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "post_purchases",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_buyer_post",
                columnNames = {"buyer_id", "post_id"})  // 같은 글 중복 구매 방지
)
public class PostPurchase extends BaseTimeEntity {

    public enum Status { COMPLETED, REFUNDED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    /** 구매 시점 가격 스냅샷 (이후 글 가격이 바뀌어도 기록 보존) */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Builder
    public PostPurchase(Long buyerId, Long postId, BigDecimal price) {
        this.buyerId = buyerId;
        this.postId = postId;
        this.price = price;
        this.status = Status.COMPLETED;
    }

    public boolean isValid() {
        return status == Status.COMPLETED;
    }

    public void refund() {
        this.status = Status.REFUNDED;
    }
}
