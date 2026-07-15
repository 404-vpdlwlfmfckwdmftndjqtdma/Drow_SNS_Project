package com.canvasflow.post.service;

import com.canvasflow.global.common.ContentVisibility;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.canvasflow.post.PostExtension;


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
    private final UserFacade userFacade;
    private final List<PostExtension> extensions;


    //글 작성
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

        //블러처리
        Map<String, Object> extensionData = postRequestDto.extensions() != null ? postRequestDto.extensions() : Map.of();
        for(PostExtension extension : extensions) {
            extension.apply(postEntity.getPostId(), extensionData.get(extension.key()));
        }

        return postEntity;
    }

    //글 목록 불러오기
    // viewerId: 지금 목록을 보고 있는 사람. 로그인 안 했으면 컨트롤러에서 null로 넘어올 수 있음
    // repository 쿼리 단계에서 delete, PRIVATE 둘 다 걸러준다
    @Transactional(readOnly = true)
    public List<PostViewDto> getAllPosts(Long viewerId, String activity) {

        List<PostEntity> posts = switch (activity == null ? "" : activity.toLowerCase()) {
            case "likedbyme" -> {
                if (viewerId == null) {
                    throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
                }
                yield postRepository.findVisiblePostsLikedByUser(viewerId);
            }
            case "commentedbyme" -> {
                if (viewerId == null) {
                    throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
                }
                yield postRepository.findVisiblePostsCommentedByUser(viewerId);
            }
            default -> postRepository.findVisiblePosts(viewerId);
        };

        List<Long> postIds = posts.stream().map(PostEntity::getPostId).toList();
        Map<Long, List<PostRequestDto.MediaItem>> mediaByPostId = postIds.isEmpty()
                ? Map.of()
                // postId별 media 리스트로 묶어서 각 게시글에 붙일 수 있게 함
                : postMediaRepository.findByPostIdInOrderByPostIdAscSortOrderAsc(postIds).stream()
                        .collect(Collectors.groupingBy(
                                PostMediaEntity::getPostId,
                                Collectors.mapping(
                                        m -> new PostRequestDto.MediaItem(m.getUrl(), m.getMediaType()),
                                        Collectors.toList()
                                )
                        ));

        // 작성자별 닉네임도 media처럼 배치로 한 번에 조회 (글마다 따로 조회하면 N+1)
        List<Long> authorIds = posts.stream().map(PostEntity::getUserId).distinct().toList();
        Map<Long, String> nicknameByUserId = userFacade.findNicknamesByIds(authorIds);

        return posts.stream()
                .map(post -> {
                    // 상세 조회와 동일하게, 목록에서도 블러 등 모듈 렌더 파이프라인을 거쳐야 원문이 새지 않는다
                    String renderedContent = post.getContent();
                    for (PostExtension extension : extensions) {
                        renderedContent = extension.render(post.getPostId(), renderedContent);
                    }
                    return new PostViewDto(
                            post.getUserId(),
                            post.getPostId(),
                            renderedContent,
                            post.getVisibility(),
                            List.copyOf(post.getTags()),
                            mediaByPostId.getOrDefault(post.getPostId(), List.of()),
                            post.getViewCount(),
                            post.getCreatedAt(),
                            post.getUpdatedAt(),
                            nicknameByUserId.get(post.getUserId())
                    );
                })
                .toList();
    }


    //글 상세 페이지
    @Transactional
    public PostViewDto getDetail(Long viewerId, Long postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));

        if(post.getDeletedAt() != null){
            throw new CanvasflowException(ErrorCode.POST_NOT_FOUND);
        }

        if(post.getVisibility() == ContentVisibility.PRIVATE && !post.getUserId().equals(viewerId)){
            throw new CanvasflowException(ErrorCode.POST_NOT_FOUND);
        }

        String renderedContent = post.getContent();
        for (PostExtension extension : extensions) {
            renderedContent = extension.render(post.getPostId(), renderedContent);
        }

        post.increaseViewCount();

        List<PostRequestDto.MediaItem> mediaItems = postMediaRepository
                .findByPostIdInOrderByPostIdAscSortOrderAsc(List.of(postId)).stream()
                .map(m-> new PostRequestDto.MediaItem(m.getUrl(), m.getMediaType()))
                .toList();

        return new PostViewDto(
                post.getUserId(),
                post.getPostId(),
                renderedContent,
                post.getVisibility(),
                List.copyOf(post.getTags()),
                mediaItems,
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                userFacade.findNicknameById(post.getUserId())
        );
    }

    //게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, PostRequestDto postRequestDto){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));

        if(post.getDeletedAt() != null){
            throw new CanvasflowException(ErrorCode.POST_NOT_FOUND);
        }

        if(!post.getUserId().equals(userId)){
            throw new CanvasflowException(ErrorCode.FORBIDDEN);
        }

        boolean hasContent = postRequestDto.content() != null && !postRequestDto.content().isBlank();
        boolean hasMedia = postRequestDto.media() != null && !postRequestDto.media().isEmpty();
        if (!hasContent && !hasMedia) {
            throw new CanvasflowException(ErrorCode.POST_CONTENT_REQUIRED);
        }

        post.update(postRequestDto.content(), postRequestDto.visibility(), postRequestDto.tags());

        //블러처리
        Map<String, Object> extensionData = postRequestDto.extensions() != null ? postRequestDto.extensions() : Map.of();
        for(PostExtension extension : extensions) {
            extension.apply(post.getPostId(), extensionData.get(extension.key()));
        }

        //이미지 수정은 지우고 새로 저장 방식으로(프론트에서는 기존의 사진들도 떠 추가/삭제 가능, 서버에서는 지우고 새로 채워넣는 방식)
        postMediaRepository.deleteAllByPostId(postId);
        List<PostRequestDto.MediaItem> mediaItems = postRequestDto.media();
        if (mediaItems != null && !mediaItems.isEmpty()) {
            List<PostMediaEntity> mediaEntities = new ArrayList<>();
            for (int i = 0; i < mediaItems.size(); i++) {
                PostRequestDto.MediaItem item = mediaItems.get(i);
                mediaEntities.add(PostMediaEntity.builder()
                        .postId(postId)
                        .url(item.url())
                        .mediaType(item.mediaType())
                        .sortOrder(i)
                        .build());
            }
            postMediaRepository.saveAll(mediaEntities);
        }

    }

    //삭제
    @Transactional
    public void deletePost(Long userId, Long postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));

        if(post.getDeletedAt() != null){
            throw new CanvasflowException(ErrorCode.POST_NOT_FOUND);
        }

        if(!post.getUserId().equals(userId)){
            throw new CanvasflowException(ErrorCode.FORBIDDEN);
        }

        post.delete();
    }









}
