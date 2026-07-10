package com.canvasflow.comment.entity;

/**
 * ACTIVE = 정상 노출.
 * DELETED = 자식(대댓글)이 있어 하드삭제 대신 상태만 바꾼 경우. 화면엔 "삭제된 댓글입니다"로 표시하고
 * 본문은 API 응답에도 내려주지 않는다 (CommentResponse.of 참고).
 */
public enum CommentStatus {
    ACTIVE,
    DELETED
}
