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

    // mypage 모듈이 마이페이지 "창작물" 통계(postCount)를 채우려고 추가함 - post 담당자 확인 부탁드립니다.
    // (구현은 PostReaderImpl.countByAuthorId 참고, PostRepository.countByUserIdAndDeletedAtIsNull 사용)
    long countByAuthorId(Long userId);

    record PostPurchaseInfo(Long authorId, BigDecimal singlePurchasePrice) {}

    record PostInfo(Long authorId) {}
}
