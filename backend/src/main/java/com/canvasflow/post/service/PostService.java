package com.canvasflow.post.service;

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
    private final PostViewAssembler postViewAssembler;


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

    //글 목록 불러오기 (공개 피드)
    // viewerId: 지금 목록을 보고 있는 사람. 로그인 안 했으면 컨트롤러에서 null로 넘어올 수 있음
    // "내가 좋아요/댓글 단 글" 목록은 이 메서드가 아니라 mypage가 PostReader.getViewablePosts로 조합한다.
    @Transactional(readOnly = true)
    public List<PostViewDto> getAllPosts(Long viewerId) {

        // 1) 재료 준비: 삭제(soft delete) 안 된 글을 최신순으로 전부 가져온다. 아직 원문 그대로인 "날것" 상태.
        List<PostEntity> posts = postRepository.findVisiblePosts();

        // 2) 가공: media·닉네임을 배치로 붙이고, 블러 등 렌더 파이프라인을 거쳐
        //    viewerId에게 보여줘도 안전한 형태(비구독자는 블러 구간 ● 치환)로 조립해서 반환한다.
        return postViewAssembler.toViewDtos(posts, viewerId);
    }


    //글 상세 페이지
    @Transactional
    public PostViewDto getDetail(Long viewerId, Long postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));

        if(post.getDeletedAt() != null){
            throw new CanvasflowException(ErrorCode.POST_NOT_FOUND);
        }

        post.increaseViewCount();

        List<PostRequestDto.MediaItem> mediaItems = postMediaRepository
                .findByPostIdInOrderByPostIdAscSortOrderAsc(List.of(postId)).stream()
                .map(m-> new PostRequestDto.MediaItem(m.getUrl(), m.getMediaType()))
                .toList();

        // 카트에 싣고 → 렌더 파이프라인(구독확인→본문블러→첨부블러) 통과 → 완성품으로 봉인
        PostViewContent content = PostViewContent.from(
                post,
                mediaItems,
                userFacade.findNicknameById(post.getUserId()),
                userFacade.getProfileView(post.getUserId()).profileImageUrl());

        postViewAssembler.renderForViewer(content, viewerId);

        return content.toDto();
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
