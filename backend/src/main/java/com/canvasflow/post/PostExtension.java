package com.canvasflow.post;

import com.canvasflow.global.media.MediaType;

import java.util.List;

/**
 * [post 모듈의 창구 - 유일하게 노출되는 타입]
 *
 * 블러/워터마크 같은 기능 모듈은 이 인터페이스만 의존해 post 에 꽂힌다.
 * post 의 나머지(엔티티/리포지토리/서비스)는 internal 에 숨겨져 있어 못 건드린다.
 *
 * @Component 를 붙이면 Spring 이 List<PostExtension> 으로 주입 → post 는 구현체를 모른다. (DIP)
 */
public interface PostExtension {

    /** 요청 extensions 에서 이 기능이 꺼내갈 칸 이름 (예: "textBlur"). */
    String key();

    /**
     * 게시글 저장 직후 호출. postId(방금 저장된 글) + 자기 칸 데이터(section)만 받는다.
     * section 은 Jackson 이 만든 Map (예: {"ranges":[{"start":9,"end":15}]}) 또는 null.
     * 각 기능은 이걸 파싱해 자기 테이블에 저장한다.
     */
    void apply(Long postId, Object section);

    /**
     * 게시글 조회 시 본문 텍스트를 가공할 기회. 기본은 그대로 통과.
     * authorId(작성자)/viewerId(지금 보는 사람)를 같이 받아서, 작성자 본인/구매자/구독자면
     * 원문을 그대로 돌려주는 식의 판단을 모듈이 직접 할 수 있게 한다.
     * 예) 텍스트 블러는 자기 테이블의 구간을 읽어 ● 로 치환한 텍스트를 반환한다.
     * 여러 기능이 있으면 순서대로 통과하며 가공된다(파이프라인).
     */
    default String render(Long postId, Long authorId, Long viewerId, String text) {
        return text;
    }

    /** post 내부 DTO(PostRequestDto 등)를 다른 모듈에 노출하지 않기 위한, 확장 모듈 전용 공개 미디어 타입. */
    record MediaItem(String url, MediaType mediaType) {}

    /**
     * 게시글 조회 시 미디어 목록을 가공할 기회. 기본은 그대로 통과.
     * 예) 이미지 블러는 지정된 사진의 url을 블러 처리된 버전으로 바꿔치기한다.
     */
    default List<MediaItem> renderMedia(Long postId, Long authorId, Long viewerId, List<MediaItem> media) {
        return media;
    }
}
