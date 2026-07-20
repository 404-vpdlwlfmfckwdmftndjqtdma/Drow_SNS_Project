package com.canvasflow.post.service;

import com.canvasflow.global.media.MediaType;
import com.canvasflow.post.PostReader;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostRepository;
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

    // mypage 모듈이 포트폴리오 그리드용으로 추가함 - post 담당자 확인 부탁드립니다.
    // PostService.getAllPosts와 같은 방식(postId 목록으로 media를 한 번에 조회)으로 N+1을 피하고,
    // 각 게시글의 media 중 sortOrder가 가장 앞선 것만 썸네일로 골라서 내려준다.
    @Override
    public List<PostSummary> getPostsByAuthorId(Long userId) {
        List<PostEntity> posts = postRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(PostEntity::getPostId).toList();
        Map<Long, PostMediaEntity> firstMediaByPostId = postMediaRepository
                .findByPostIdInOrderByPostIdAscSortOrderAsc(postIds)
                .stream()
                .collect(Collectors.toMap(
                        PostMediaEntity::getPostId,
                        media -> media,
                        (existing, replacement) -> existing.getSortOrder() <= replacement.getSortOrder() ? existing : replacement
                ));

        return posts.stream()
                .map(post -> {
                    PostMediaEntity firstMedia = firstMediaByPostId.get(post.getPostId());
                    return new PostSummary(
                            post.getPostId(),
                            post.getContent(),
                            firstMedia != null ? firstMedia.getUrl() : null,
                            firstMedia != null && firstMedia.getMediaType() == MediaType.VIDEO,
                            post.getCreatedAt()
                    );
                })
                .toList();
    }

    // mypage 모듈이 마이페이지 "조회수" 통계용으로 추가함 - post 담당자 확인 부탁드립니다.
    @Override
    public long sumViewCountByAuthorId(Long userId) {
        return postRepository.sumViewCountByUserId(userId);
    }
}
