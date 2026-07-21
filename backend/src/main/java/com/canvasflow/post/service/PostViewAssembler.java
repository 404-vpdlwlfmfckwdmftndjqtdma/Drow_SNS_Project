package com.canvasflow.post.service;

import com.canvasflow.post.ContentAccessPolicy;
import com.canvasflow.post.PostExtension;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 목록/상세 공통 조립부: media·닉네임 배치 조회 + 블러 등 확장 모듈 렌더 파이프라인.
 * 게시글이 어떤 경로로 나가든(전체 피드, 상세, PostReader 공개 창구) 반드시 여기를
 * 거쳐야 원문이 새지 않는다. 렌더 로직은 이 클래스 한 곳에만 둔다.
 */
@Component
@RequiredArgsConstructor
public class PostViewAssembler {

    private final PostMediaRepository postMediaRepository;
    private final UserFacade userFacade;
    private final List<PostExtension> extensions;
    // entitlement 도메인이 아직 구현체를 안 올렸을 수도 있어 Optional로 받는다 - 없으면 전부 잠금 상태로 취급.
    private final Optional<ContentAccessPolicy> contentAccessPolicy;

    // 목록 공통 조립: 글 1개당 카트(PostViewContent) 1개. 넘어온 posts 순서를 그대로 유지한다.
    public List<PostViewDto> toViewDtos(List<PostEntity> posts, Long viewerId) {

        if (posts.isEmpty()) {
            return List.of();
        }

        // 카트에 실을 재료는 미리 한 방에 조회해 둔다 (루프 안에서 글마다 조회하면 N+1)
        Map<Long, List<PostRequestDto.MediaItem>> mediaByPostId = loadMediaByPostId(posts);
        Map<Long, UserProfileView> profileByAuthorId = loadProfileByAuthorId(posts);

        // 글 1개당: 카트에 싣고 → 렌더 파이프라인 통과 → 완성품으로 봉인
        List<PostViewDto> result = new ArrayList<>();
        for (PostEntity post : posts) {
            UserProfileView profile = profileByAuthorId.get(post.getUserId());
            PostViewContent content = PostViewContent.from(
                    post,
                    mediaByPostId.getOrDefault(post.getPostId(), List.of()),
                    profile != null ? profile.nickname() : null,
                    profile != null ? profile.profileImageUrl() : null);

            renderForViewer(content, viewerId);

            result.add(content.toDto());
        }
        return result;
    }

    // [재료 준비] 첨부를 IN 쿼리 한 방으로 조회해서 postId별 서랍(Map)에 나눠 담는다.
    // 결과: { 글17: [사진A, 사진B], 글9: [영상C] }
    private Map<Long, List<PostRequestDto.MediaItem>> loadMediaByPostId(List<PostEntity> posts) {
        List<Long> postIds = new ArrayList<>();
        for (PostEntity post : posts) {
            postIds.add(post.getPostId());
        }

        Map<Long, List<PostRequestDto.MediaItem>> mediaByPostId = new HashMap<>();
        for (PostMediaEntity media : postMediaRepository.findByPostIdInOrderByPostIdAscSortOrderAsc(postIds)) {
            List<PostRequestDto.MediaItem> drawer = mediaByPostId.get(media.getPostId());
            if (drawer == null) {                      // 이 글의 서랍이 아직 없으면 새로 만든다
                drawer = new ArrayList<>();
                mediaByPostId.put(media.getPostId(), drawer);
            }
            drawer.add(new PostRequestDto.MediaItem(media.getUrl(), media.getMediaType()));
        }
        return mediaByPostId;
    }

    // [재료 준비] 작성자 닉네임을 한 방에 조회한다. 결과: { 유저5: "화가딩", 유저7: "글쟁이" }
    // 프로필 사진도 같이 조회
    private Map<Long, UserProfileView> loadProfileByAuthorId(List<PostEntity> posts) {
        Set<Long> authorIds = new LinkedHashSet<>();   // Set이라 같은 작성자는 한 번만 담긴다
        for (PostEntity post : posts) {
            authorIds.add(post.getUserId());
        }
        return userFacade.getProfileViews(List.copyOf(authorIds));
    }

    /**
     * [렌더 파이프라인] 카트에 실린 글 한 건을 열람자에게 안전한 형태로 가공한다.
     * 어느 경로(피드/상세/PostReader)로 나가든 반드시 이 메서드를 거쳐야 원문이 새지 않는다.
     */
    public void renderForViewer(PostViewContent content, Long viewerId) {

        // [1단계] 구독·개별구매 확인 - entitlement에 "이 열람자가 이 글에서 잠금 해제한
        //         모듈 key 집합"(예: {"imageBlur"})을 물어본다. 정책 미구현이면 전부 잠금(fail-closed).
        Set<String> unlockedKeys = checkUnlockedModules(viewerId, content.postId(), content.authorId());

        // [2단계] 본문 가공 - 텍스트 블러 등 확장 모듈을 차례로 통과 (잠금 해제된 모듈은 원문 유지)
        content.replaceText(renderText(content.postId(), content.text(), unlockedKeys));

        // [3단계] 첨부 가공 - 이미지 블러/워터마크 등 (잠금 해제된 모듈은 원본 그대로 통과)
        content.replaceMedia(renderMedia(content.postId(), unlockedKeys, content.media()));
    }

    // [1단계 구현] ContentAccessPolicy(entitlement 구현)가 있으면 물어보고, 없으면 빈 set = 전부 잠금
    private Set<String> checkUnlockedModules(Long viewerId, Long postId, Long authorId) {
        return contentAccessPolicy
                .map(policy -> policy.unlockedKeys(viewerId, postId, authorId))
                .orElse(Set.of());
    }

    // [2단계 구현] 본문을 확장 모듈(텍스트 블러 등)에 순서대로 통과시킨다.
    // 각 모듈에는 "네 잠금이 풀렸는지"를 함께 알려준다 - 풀렸으면 모듈이 원문을 그대로 돌려준다.
    private String renderText(Long postId, String text, Set<String> unlockedKeys) {
        String rendered = text;
        for (PostExtension extension : extensions) {
            rendered = extension.render(postId, rendered, unlockedKeys.contains(extension.key()));
        }
        return rendered;
    }

    // [3단계 구현] 첨부를 확장 모듈(이미지 블러 등)에 통과시킨다.
    // PostRequestDto.MediaItem(내부 DTO) <-> PostExtension.MediaItem(공개 타입) 변환을 감춰서
    // 확장 모듈이 post의 내부 타입을 직접 참조하지 않도록 한다
    private List<PostRequestDto.MediaItem> renderMedia(Long postId, Set<String> unlockedKeys, List<PostRequestDto.MediaItem> media) {
        List<PostExtension.MediaItem> converted = media.stream()
                .map(m -> new PostExtension.MediaItem(m.url(), m.mediaType()))
                .collect(Collectors.toList());
        for (PostExtension extension : extensions) {
            converted = extension.renderMedia(postId, converted, unlockedKeys.contains(extension.key()));
        }
        return converted.stream()
                .map(m -> new PostRequestDto.MediaItem(m.url(), m.mediaType()))
                .toList();
    }
}
