package com.canvasflow.domain.post.service;

import com.canvasflow.domain.post.dto.*;
import com.canvasflow.domain.post.entity.Post;
import com.canvasflow.domain.post.entity.PostMedia;
import com.canvasflow.domain.post.repository.PostRepository;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.media.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    // TODO: ContentAccessService(subscription 도메인) 주입 -> 상세 조회 시 구독 여부에 따른 locked 판정

    @Transactional
    public Long create(Long authorId, PostCreateRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));

        // TODO: channelId 있으면 ChannelRepository 로 조회해서 세팅
        Post post = Post.builder()
                .author(author)
                .title(request.title())
                .content(request.content())
                .visibility(request.visibility())
                .tags(request.tags())
                .build();

        if (request.mediaList() != null) {
            request.mediaList().forEach(m ->
                    post.addMedia(PostMedia.builder()
                            .url(m.url())
                            .mediaType(MediaType.valueOf(m.mediaType()))
                            .sortOrder(m.sortOrder())
                            .build())
            );
        }

        return postRepository.save(post).getId();
    }

    @Transactional
    public PostResponse getDetail(Long postId, Long viewerId) {
        Post post = getPostOrThrow(postId);
        postRepository.increaseViewCount(postId);

        // TODO: viewerId 기준 구독/팔로우 여부로 locked 판정 (subscription 도메인 연동)
        boolean locked = false;
        return PostResponse.from(post, locked);
    }

    @Transactional
    public void update(Long postId, Long authorId, PostUpdateRequest request) {
        Post post = getPostOrThrow(postId);
        // TODO: 작성자 본인 검증 (post.getAuthor().getId().equals(authorId)) 후 아니면 ErrorCode.FORBIDDEN
        post.update(request.title(), request.content(), request.visibility());
    }

    @Transactional
    public void delete(Long postId, Long authorId) {
        Post post = getPostOrThrow(postId);
        // TODO: 작성자 본인 검증
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> search(PostSearchCondition condition, Pageable pageable) {
        return postRepository.search(condition, pageable);
    }

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));
    }
}
