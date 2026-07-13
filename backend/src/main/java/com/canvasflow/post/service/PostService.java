package com.canvasflow.post.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * [핵심] 게시글 저장 파이프라인. core 와 기능 모듈이 만나는 유일한 지점.
 *
 * Spring 이 @Component 붙은 PostExtension 구현체 전부를 List 로 주입한다. (컬렉션 주입)
 * → 기능 모듈이 늘어나도 이 클래스는 수정 0줄. (DIP / OCP)
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;


    @Transactional
    public PostEntity createPost(Long userId, PostRequestDto postRequestDto){
        boolean hasContent = postRequestDto.content() != null && !postRequestDto.content().isBlank();
        boolean hasMedia = postRequestDto.media() != null && !postRequestDto.media().isEmpty();
        if (!hasContent && !hasMedia) {
            throw new CanvasflowException(ErrorCode.POST_CONTENT_REQUIRED);
        }

        PostEntity postEntity = postRepository.save(new PostEntity(userId, postRequestDto.content(), postRequestDto.visibility(), postRequestDto.tags()));

        //이미지 저장
        List<PostRequestDto.MediaItem> mediaItems = postRequestDto.media();
        if (mediaItems != null && !mediaItems.isEmpty()) {
            List<PostMediaEntity> mediaEntities = new ArrayList<>();
            for (int i = 0; i < mediaItems.size(); i++) {
                PostRequestDto.MediaItem item = mediaItems.get(i);
                mediaEntities.add(PostMediaEntity.builder()
                        .postId(postEntity.getPostId())
                        .url(item.url())
                        .mediaType(item.mediaType())
                        .sortOrder(i)
                        .build());
            }
            postMediaRepository.saveAll(mediaEntities);
        }

        return postEntity;
    }

    @Transactional(readOnly = true)
    public List<PostViewDto> getAllPosts() {
        List<PostEntity> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Long> postIds = posts.stream().map(PostEntity::getPostId).toList();
        Map<Long, List<PostRequestDto.MediaItem>> mediaByPostId = postIds.isEmpty()
                ? Map.of()
                // postId별 media 리스트로 묶어서 각 게시글에 붙일 수 있게 준비
                : postMediaRepository.findByPostIdInOrderByPostIdAscSortOrderAsc(postIds).stream()
                        .collect(Collectors.groupingBy(
                                PostMediaEntity::getPostId,
                                Collectors.mapping(
                                        m -> new PostRequestDto.MediaItem(m.getUrl(), m.getMediaType()),
                                        Collectors.toList()
                                )
                        ));

        return posts.stream()
                .map(post -> new PostViewDto(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getVisibility(),
                        List.copyOf(post.getTags()),
                        mediaByPostId.getOrDefault(post.getPostId(), List.of()),
                        post.getViewCount(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();
    }





}
