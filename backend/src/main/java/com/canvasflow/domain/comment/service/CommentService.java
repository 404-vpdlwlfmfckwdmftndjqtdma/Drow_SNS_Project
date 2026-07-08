package com.canvasflow.domain.comment.service;

import com.canvasflow.domain.comment.dto.CommentCreateRequest;
import com.canvasflow.domain.comment.entity.Comment;
import com.canvasflow.domain.comment.repository.CommentRepository;
import com.canvasflow.domain.post.entity.Post;
import com.canvasflow.domain.post.repository.PostRepository;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    // TODO: NotificationService 주입 -> 댓글 작성 시 게시글 작성자에게 알림

    @Transactional
    public Long create(Long postId, Long authorId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.POST_NOT_FOUND));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder().post(post).author(author).content(request.content()).build();
        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public Page<Comment> getByPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    @Transactional
    public void update(Long commentId, Long authorId, CommentCreateRequest request) {
        Comment comment = getCommentOrThrow(commentId);
        // TODO: 작성자 본인 검증
        comment.update(request.content());
    }

    @Transactional
    public void delete(Long commentId, Long authorId) {
        Comment comment = getCommentOrThrow(commentId);
        // TODO: 작성자 본인 검증
        commentRepository.delete(comment);
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
