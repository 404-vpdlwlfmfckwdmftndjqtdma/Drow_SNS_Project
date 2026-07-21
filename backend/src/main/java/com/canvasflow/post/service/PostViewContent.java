package com.canvasflow.post.service;

import com.canvasflow.global.common.ContentVisibility;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [조회 파이프라인의 운반 객체(카트)]
 * "가공 중인 게시글 한 건"을 싣고 렌더 단계들을 통과한다.
 *
 * 흐름:  from(날것 싣기) → 렌더 파이프라인 통과(가공) → toDto(완성품 봉인)
 *
 * 가공되는 것은 본문(text)과 첨부(media) 둘뿐이고, 나머지 필드는 final로 잠가서
 * 파이프라인 도중 바뀔 수 없게 했다.
 * post 내부 전용(package-private) - 확장 모듈에는 절대 통째로 넘기지 않는다(조각만 꺼내 전달).
 */
class PostViewContent {

    // 가공 대상이 아닌 정보 - 실은 채로 변하지 않는다
    private final Long postId;
    private final Long authorId;
    private final ContentVisibility visibility;
    private final List<String> tags;
    private final Long viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String nickname;
    private final String profileImageUrl;

    // 가공 대상 - 렌더 파이프라인이 블러본으로 교체해 간다
    private String text;
    private List<PostRequestDto.MediaItem> media;

    private PostViewContent(Long postId, Long authorId, ContentVisibility visibility,
                            List<String> tags, Long viewCount,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            String nickname, String profileImageUrl,
                            String text, List<PostRequestDto.MediaItem> media) {
        this.postId = postId;
        this.authorId = authorId;
        this.visibility = visibility;
        this.tags = tags;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.text = text;
        this.media = media;
    }

    /** 날것 싣기: 엔티티 + 미리 조회해 둔 첨부/닉네임/프로필사진으로 카트를 채운다. */
    static PostViewContent from(PostEntity post, List<PostRequestDto.MediaItem> media,
                                String nickname, String profileImageUrl) {
        return new PostViewContent(
                post.getPostId(),
                post.getUserId(),
                post.getVisibility(),
                List.copyOf(post.getTags()),   // 지연 로딩 컬렉션은 세션 열려 있을 때 복사
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                nickname,
                profileImageUrl,
                post.getContent(),             // 아직 원문 - 반드시 렌더 파이프라인을 거칠 것
                media
        );
    }

    /** 완성품 봉인: 가공이 끝난 상태를 불변 DTO로 굳혀서 내보낸다. */
    PostViewDto toDto() {
        return new PostViewDto(
                authorId,
                postId,
                text,
                visibility,
                tags,
                media,
                viewCount,
                createdAt,
                updatedAt,
                nickname,
                profileImageUrl
        );
    }

    // --- 렌더 파이프라인이 쓰는 최소한의 창구 ---

    Long postId() {
        return postId;
    }

    Long authorId() {
        return authorId;
    }

    String text() {
        return text;
    }

    List<PostRequestDto.MediaItem> media() {
        return media;
    }

    void replaceText(String renderedText) {
        this.text = renderedText;
    }

    void replaceMedia(List<PostRequestDto.MediaItem> renderedMedia) {
        this.media = renderedMedia;
    }
}
