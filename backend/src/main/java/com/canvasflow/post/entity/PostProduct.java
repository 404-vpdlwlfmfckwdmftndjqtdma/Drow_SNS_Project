package com.canvasflow.post.entity;

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

import java.math.BigDecimal;

/**
 * [상품 가격표] 글 하나에서 판매자가 정한 "기능별 잠금 해제 가격".
 *
 * 예) 글17 → textBlur 5000원, imageBlur 3000원
 * capability 는 PostExtension.key() 와 1:1 매칭되는 문자열이라,
 * 새 블러 모듈이 생겨도 행만 추가하면 되고 스키마는 그대로다.
 *
 * 구매 "기록"인 purchase_items 와 혼동 주의:
 *   이 테이블 = 팔기 전 가격표(구매자 없음), purchase_items = 산 뒤 권한(구매자 있음).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "post_products",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_capability",
                columnNames = {"post_id", "capability"}),   // 같은 글에 같은 기능 가격 중복 금지
        indexes = @Index(name = "idx_pp_post", columnList = "post_id")
)
public class PostProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    /** 판매하는 기능 key (PostExtension.key() 와 매칭). 예: "textBlur" */
    @Column(nullable = false, length = 50)
    private String capability;

    /** 판매가(원). 0이면 무료 공개와 같으므로 등록 시 양수만 허용한다. */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    /** 판매 중지(글은 그대로 두고 판매만 내림). 내려도 이미 구매한 사람의 권한은 유지된다. */
    @Column(name = "on_sale", nullable = false)
    private boolean onSale;

    public PostProduct(Long postId, String capability, BigDecimal price) {
        this.postId = postId;
        this.capability = capability;
        this.price = price;
        this.onSale = true;
    }

    public void changePrice(BigDecimal price) {
        this.price = price;
    }

    public void stopSelling() {
        this.onSale = false;
    }
}
