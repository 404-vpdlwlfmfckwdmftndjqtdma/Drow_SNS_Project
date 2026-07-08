package com.canvasflow.domain.post.extension;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 조회 파이프라인을 흐르는 "가공 중인 게시글 내용물".
 * core 가 DB 원본으로 만들어서 각 모듈의 processView() 에 차례로 넘기고,
 * 모듈은 자기 담당 부분만 변경한다. (예: 텍스트 블러 → content 의 특정 구간을 ● 로 치환)
 *
 * extensions: 모듈이 프론트 렌더링에 필요한 메타데이터를 자기 key 로 담는 곳.
 * 예) extensions.put("textBlur", Map.of("ranges", [...], "unlocked", false))
 */
@Getter
@Setter
public class PostViewContent {

    private final Long postId;
    private final Long authorId;

    /** 본문 텍스트. 모듈이 마스킹 등으로 교체할 수 있어 가변. */
    private String content;

    /** 미디어 URL 목록. 이미지 블러 모듈이 블러본 URL 로 교체하는 식으로 사용. */
    private List<String> mediaUrls;

    /** 모듈별 프론트 전달용 메타데이터. key = PostModule.key() */
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    public PostViewContent(Long postId, Long authorId, String content, List<String> mediaUrls) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.mediaUrls = mediaUrls;
    }
}
