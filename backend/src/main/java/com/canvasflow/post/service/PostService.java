package com.canvasflow.post.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.post.FollowingPolicy;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.entity.PostProduct;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostProductRepository;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
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
    private final PostProductRepository postProductRepository;
    private final UserFacade userFacade;
    private final List<PostExtension> extensions;
    private final PostViewAssembler postViewAssembler;

    //피드목록에서 팔로워의 글이 먼저 뜨도록. Optional로 처리해 코드 구현 없어도 터지지 않게 함
    private final Optional<FollowingPolicy> followingPolicy;


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

        //판매 가격표 저장 (판매자가 기능별로 값을 매긴 경우)
        replaceProducts(postEntity.getPostId(), postRequestDto.prices());

        return postEntity;
    }

    /**
     * 가격표 전체 교체. 요청에 없는 기능은 판매하지 않는 것으로 본다(글 수정 시 전체 전송 규칙).
     * 이미 구매한 사람의 권한(purchase_items)은 가격표와 별개라 여기서 지워도 영향이 없다.
     */
    private void replaceProducts(Long postId, Map<String, BigDecimal> prices) {
        postProductRepository.deleteAllByPostId(postId);
        if (prices == null || prices.isEmpty()) {
            return;
        }
        List<PostProduct> products = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            BigDecimal price = entry.getValue();
            if (price == null || price.signum() <= 0) {
                continue;   // 0원 이하는 판매 안 함으로 간주 (무료 공개와 같음)
            }
            products.add(new PostProduct(postId, entry.getKey(), price));
        }
        postProductRepository.saveAll(products);
    }

    //글 목록 불러오기 (공개 피드)
    // viewerId: 지금 목록을 보고 있는 사람. 로그인 안 했으면 컨트롤러에서 null로 넘어올 수 있음
    // "내가 좋아요/댓글 단 글" 목록은 이 메서드가 아니라 mypage가 PostReader.getViewablePosts로 조합한다.
    @Transactional(readOnly = true)
    public List<PostViewDto> getAllPosts(Long viewerId) {

        //팔로우 한 사람의 게시글이 먼저 보이도록
        Set<Long> followingIds = followingPolicy
                .map(policy -> policy.followingIds(viewerId))
                .orElse(Set.of());
        //나 자신도 팔로우 한 사람에 포함
        if (viewerId != null) {
            followingIds = new HashSet<>(followingIds);
            followingIds.add(viewerId);
        }

        // 1) 재료 준비: 삭제(soft delete) 안 된 글을 팔로우 우선순위+최신순으로 가져온다. 아직 원문 그대로인 "날것" 상태.
        List<PostEntity> posts = postRepository.findVisiblePosts(followingIds);

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

        // 블러/가격표는 "보낸 경우에만" 교체한다.
        // 요청에 아예 없으면(null) 손대지 않는다 - 예전에는 없으면 전부 삭제로 처리해서,
        // extensions를 보내지 않는 수정 화면에서 글만 고쳐도 블러가 통째로 사라졌다.
        // 지우고 싶으면 빈 값({} 또는 [])을 명시적으로 보내면 된다.
        if (postRequestDto.extensions() != null) {
            Map<String, Object> extensionData = postRequestDto.extensions();
            for (PostExtension extension : extensions) {
                extension.apply(post.getPostId(), extensionData.get(extension.key()));
            }
        }

        if (postRequestDto.prices() != null) {
            replaceProducts(postId, postRequestDto.prices());
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
