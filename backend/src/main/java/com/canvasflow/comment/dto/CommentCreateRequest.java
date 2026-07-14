package com.canvasflow.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 원댓글/대댓글 작성 요청. 엔드포인트를 따로 나누지 않고 parentId 유무로만 구분한다
 * (둘 다 "이 게시글에 댓글 하나 추가"라는 동작이라 굳이 나눌 실익이 없음).
 * parentId가 이미 대댓글인 경우는 CommentService에서 검증 후 거부한다.
 */
public record CommentCreateRequest(
        @NotBlank @Size(max = 1000) String content,
        Long parentId // null이면 원댓글, 값이 있으면 그 원댓글에 대한 대댓글
) {
}
