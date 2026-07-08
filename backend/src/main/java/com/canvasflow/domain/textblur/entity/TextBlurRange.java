package com.canvasflow.domain.textblur.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [textblur 모듈 소유 테이블]
 * 본문에서 블러 처리할 구간(문자 오프셋). 배열 원소 하나 = 행 하나로 분해 저장.
 *
 * core 의 posts 테이블에는 컬럼을 추가하지 않고 post_id 로만 참조한다.
 * → 이 모듈을 삭제하면 이 테이블만 DROP 하면 끝. (모듈 소유권 원칙)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "text_blur_ranges", indexes = @Index(name = "idx_text_blur_post", columnList = "post_id"))
public class TextBlurRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    /** 블러 시작 위치 (본문 문자 인덱스, 포함) */
    @Column(nullable = false)
    private int startIdx;

    /** 블러 끝 위치 (미포함, 즉 [startIdx, endIdx) 구간) */
    @Column(nullable = false)
    private int endIdx;

    public TextBlurRange(Long postId, int startIdx, int endIdx) {
        this.postId = postId;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }
}
