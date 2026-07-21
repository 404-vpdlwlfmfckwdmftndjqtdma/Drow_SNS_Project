package com.canvasflow.post.service;

import com.canvasflow.global.media.MediaType;
import com.canvasflow.post.PostReader;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostProductRepository;
import com.canvasflow.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReaderImpl implements PostReader {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostProductRepository postProductRepository;
    private final PostViewAssembler assembler;

    @Override
    public Optional<PostInfo> getPostInfo(Long postId) {
        return postRepository.findById(postId)
                .map(post -> new PostInfo(post.getUserId()));
    }

    // 판매자가 글에 등록해 둔 기능별 가격표. 삭제된 글이면 빈 목록.
    @Override
    public List<ProductInfo> getProducts(Long postId) {
        Long authorId = findAuthorIdOfLivePost(postId);
        if (authorId == null) {
            return List.of();
        }
        return postProductRepository.findByPostIdAndOnSaleTrue(postId).stream()
                .map(product -> new ProductInfo(postId, authorId, product.getCapability(), product.getPrice()))
                .toList();
    }

    // 구매 직전 단건 확인. 삭제된 글 / 판매 중지 상품이면 empty → purchase 쪽에서 구매 불가 처리.
    @Override
    public Optional<ProductInfo> getProduct(Long postId, String capability) {
        Long authorId = findAuthorIdOfLivePost(postId);
        if (authorId == null) {
            return Optional.empty();
        }
        return postProductRepository.findByPostIdAndCapabilityAndOnSaleTrue(postId, capability)
                .map(product -> new ProductInfo(postId, authorId, product.getCapability(), product.getPrice()));
    }

    // 살아있는 글이면 작성자 id, 삭제됐거나 없으면 null
    private Long findAuthorIdOfLivePost(Long postId) {
        return postRepository.findById(postId)
                .filter(post -> post.getDeletedAt() == null)
                .map(PostEntity::getUserId)
                .orElse(null);
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

    // mypage의 "내가 좋아요한 글 / 댓글 단 글" 목록용 창구.
    // 렌더 파이프라인(블러 등)은 PostViewAssembler 한 곳에만 두고 여기서는 조회+순서 보존+변환만 한다.
    @Override
    public List<PostView> getViewablePosts(List<Long> postIds, Long viewerId) {
        if (postIds.isEmpty()) {
            return List.of();
        }

        List<PostEntity> posts = postRepository.findByPostIdInAndDeletedAtIsNull(postIds);

        // IN 쿼리는 순서를 보장하지 않으므로 호출자가 넘긴 id 순서(예: 좋아요 누른 순)대로 재정렬.
        // 삭제된 글의 id는 조회 결과에 없으므로 여기서 자연스럽게 걸러진다.
        Map<Long, PostEntity> byId = posts.stream()
                .collect(Collectors.toMap(PostEntity::getPostId, post -> post));
        List<PostEntity> ordered = postIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        return assembler.toViewDtos(ordered, viewerId).stream()
                .map(dto -> new PostView(
                        dto.postId(),
                        dto.userId(),
                        dto.content(),
                        dto.tags(),
                        dto.media().stream()
                                .map(m -> new ViewMedia(m.url(), m.mediaType()))
                                .toList(),
                        dto.viewCount(),
                        dto.createdAt(),
                        dto.nickname()
                ))
                .toList();
    }
}
