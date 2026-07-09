package com.canvasflow.post.internal;

import java.util.List;
import java.util.Map;

/**
 * 게시글 저장 요청. extensions = 기능별 확장 데이터 구역(key = PostExtension.key()).
 * post 코어는 extensions 내용을 해석하지 않고, 각 기능 모듈에 자기 칸만 넘긴다.
 */
public record PostSaveRequestDto(
        String text,
        List<Long> imageIds,
        List<Long> videoIds,
        Map<String, Object> extensions
) {
}
