package com.canvasflow.textblur.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextBlurRangeRepository extends JpaRepository<TextBlurRange, Long> {

    List<TextBlurRange> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}
