package com.canvasflow.domain.textblur.repository;

import com.canvasflow.domain.textblur.entity.TextBlurRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TextBlurRangeRepository extends JpaRepository<TextBlurRange, Long> {

    List<TextBlurRange> findByPostIdOrderByStartIdxAsc(Long postId);

    @Modifying(clearAutomatically = true)
    @Query("delete from TextBlurRange r where r.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
