package com.canvasflow.global.stream;

/**
 * 피드 단일 스트림에서 쓰는 경량 카운트 이벤트 payload.
 * type에 따라 likeCount/commentCount 중 하나만 채워진다.
 */
public record PostStreamEvent(
        String type,
        Long postId,
        Long likeCount,
        Long commentCount
) {
    public static PostStreamEvent postLikeCount(Long postId, long likeCount) {
        return new PostStreamEvent("POST_LIKE_COUNT", postId, likeCount, null);
    }

    public static PostStreamEvent postCommentCount(Long postId, long commentCount) {
        return new PostStreamEvent("POST_COMMENT_COUNT", postId, null, commentCount);
    }
}
