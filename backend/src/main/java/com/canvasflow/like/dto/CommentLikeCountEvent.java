package com.canvasflow.like.dto;

// 댓글 좋아요 개수 변경 브로드캐스트 페이로드. 댓글 전용 SSE 연결을 따로 열지 않고,
// 이미 열려있는 게시글의 댓글 채널(CommentEmitterRepository)에 얹어 보낸다 (브라우저 동시 연결 수 제한 회피).
public record CommentLikeCountEvent(Long commentId, long likeCount) {
}
