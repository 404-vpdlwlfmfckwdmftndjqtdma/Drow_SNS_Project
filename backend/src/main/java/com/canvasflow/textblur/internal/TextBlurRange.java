package com.canvasflow.textblur.internal;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 본문에서 블러 처리할 문자 구간 하나.
 * 예) "안녕하세요 비밀입니다" 에서 start=6, end=10 이면 "비밀입니" 가 ●●●● 로 치환.
 * (start 포함, end 미포함 - String.substring 과 동일한 규칙)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "text_blur_ranges", indexes = @Index(name = "idx_tbr_post", columnList = "postId"))
public class TextBlurRange extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    /** 블러 시작 인덱스 (포함) */
    @Column(nullable = false)
    private int startIdx;

    /** 블러 끝 인덱스 (미포함) */
    @Column(nullable = false)
    private int endIdx;

    @Builder
    public TextBlurRange(Long postId, int startIdx, int endIdx) {
        if (startIdx < 0 || endIdx <= startIdx) {
            throw new IllegalArgumentException("잘못된 블러 구간입니다: start" + startIdx + ", end=" + endIdx);
        }
        this.postId = postId;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }

}
