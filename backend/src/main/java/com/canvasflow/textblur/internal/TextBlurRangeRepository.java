package com.canvasflow.textblur.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextBlurRangeRepository extends JpaRepository<TextBlurRange, Long> {

    /** render용: 이 글의 블러 구간들 (앞에서부터 순서대로) */
    List<TextBlurRange> findByPostIdOrderByStartIdxAsc(Long postId);

    /** apply용: 글 수정 시 기존 구간을 지우고 새로 저장 */
    void deleteByPostId(Long postId);

    boolean existsByPostId(Long postId);
}
