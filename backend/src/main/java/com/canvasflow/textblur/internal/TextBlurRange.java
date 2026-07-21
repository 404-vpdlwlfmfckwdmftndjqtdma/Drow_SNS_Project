package com.canvasflow.textblur.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * [textblur 모듈 소유 테이블] 게시글 본문에서 블러 처리할 구간.
 * post 테이블에 컬럼을 추가하지 않고 postId 로만 참조한다 → 모듈로 떼어낼 수 있는 근거.
 */
@Entity
@Table(name = "text_blur_ranges", indexes = @Index(name = "idx_tbr_post", columnList = "postId"))
public class TextBlurRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private int startIdx;

    @Column(nullable = false)
    private int endIdx;

    protected TextBlurRange() {
    }

    public TextBlurRange(Long postId, int startIdx, int endIdx) {
        this.postId = postId;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }
}
