package com.canvasflow.post;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * [post 모듈의 읽기 창구]
 * 다른 모듈이 게시글 정보를 조회할 때 쓰는 인터페이스.
 */
public interface PostReader {

    Optional<PostPurchaseInfo> getPurchaseInfo(Long postId);

    Optional<PostInfo> getPostInfo(Long postId);

    record PostPurchaseInfo(Long authorId, BigDecimal singlePurchasePrice) {}

    record PostInfo(Long authorId) {}
}