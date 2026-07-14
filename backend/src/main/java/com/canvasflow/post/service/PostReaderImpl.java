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
        return  postRepository.findById(postId)
                .map(p -> new PostPurchaseInfo(p.getAuthorId(), p.getSinglePurchasePrice()));
        // TODO PostEntity에 추가 나경님과 상담要
    }

}
