package com.canvasflow.comment.dto;

import com.canvasflow.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 조회 응답. replies는 이 댓글이 원댓글일 때만 채워지고(1-depth라 대댓글의 replies는 항상 빈 리스트),
 * writerNickname은 User 리포지토리를 직접 참조해서 채운다 (다른 도메인들도 다 이렇게 직접 참조하는
 * 관례를 따름 - post처럼 internal로 숨겨져 있지 않음).
 */
public record CommentResponse(
        Long id,
        Long postId,
        Long parentId,
        Long writerId,
        String writerNickname,
        String content,
        boolean deleted,
        LocalDateTime createdAt,
        long likeCount,
        boolean likedByMe,
        List<CommentResponse> replies
) {
    public static CommentResponse of(
            Comment comment, String writerNickname, long likeCount, boolean likedByMe, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getParentId(),
                comment.getWriterId(),
                writerNickname,
                comment.isDeleted() ? null : comment.getContent(), // 삭제된 댓글은 본문을 내려보내지 않음
                comment.isDeleted(),
                comment.getCreatedAt(),
                likeCount,
                likedByMe,
                replies
        );
    }
}
