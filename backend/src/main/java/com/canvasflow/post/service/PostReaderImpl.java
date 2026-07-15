package com.canvasflow.post.service;

import com.canvasflow.post.PostReader;
import com.canvasflow.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReaderImpl implements PostReader {

    private final PostRepository postRepository;

    @Override
    public Optional<PostPurchaseInfo> getPurchaseInfo(Long postId) {
        return postRepository.findById(postId)
                .map(post -> new PostPurchaseInfo(post.getUserId(), null));
        // TODO 단건 구매 가격 정책 확정 후 PostEntity에 singlePurchasePrice를 추가한다.
    }

    @Override
    public Optional<PostInfo> getPostInfo(Long postId) {
        return postRepository.findById(postId)
                .map(post -> new PostInfo(post.getUserId()));
    }

    // mypage 모듈이 마이페이지 "창작물" 통계용으로 추가함 - post 담당자 확인 부탁드립니다.
    @Override
    public long countByAuthorId(Long userId) {
        return postRepository.countByUserIdAndDeletedAtIsNull(userId);
    }
}
