package com.canvasflow.imageblur.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageBlurRepository extends JpaRepository<ImageBlurTarget, Long> {
    List<ImageBlurTarget> findByPostId(Long postId);
    void deleteByPostId(Long postId);

}
