package com.canvasflow.comment.dto;

// 댓글 삭제 SSE 브로드캐스트 페이로드. hardDeleted=true면 클라이언트가 트리에서 노드를 완전히 제거해야 하고,
// false(소프트 삭제)면 "삭제된 댓글입니다" 상태로만 표시를 바꾸면 된다.
public record CommentDeletedEvent(Long id, boolean hardDeleted) {
}
