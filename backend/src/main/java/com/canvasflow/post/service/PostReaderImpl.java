package com.canvasflow.post.service;

import com.canvasflow.post.PostReader;
import com.canvasflow.post.PostSearchView;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReaderImpl implements PostReader {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final UserFacade userFacade;

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

    @Override
    public List<PostSearchView> findByTag(String tag) {
        var posts = postRepository.findByTag(tag);
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(p -> p.getPostId()).toList();
        Map<Long, List<PostSearchView.MediaItem>> mediaByPostId = postMediaRepository
                .findByPostIdInOrderByPostIdAscSortOrderAsc(postIds).stream()
                .collect(Collectors.groupingBy(
                        PostMediaEntity::getPostId,
                        Collectors.mapping(
                                m -> new PostSearchView.MediaItem(m.getUrl(), m.getMediaType().name()),
                                Collectors.toList()
                        )
                ));

        List<Long> authorIds = posts.stream().map(p -> p.getUserId()).distinct().toList();
        Map<Long, String> nicknames = userFacade.findNicknamesByIds(authorIds);

        return posts.stream()
                .map(p -> new PostSearchView(
                        p.getPostId(),
                        p.getUserId(),
                        nicknames.get(p.getUserId()),
                        p.getContent(),
                        List.copyOf(p.getTags()),
                        mediaByPostId.getOrDefault(p.getPostId(), List.of()),
                        p.getCreatedAt()
                ))
                .toList();
    }
}
