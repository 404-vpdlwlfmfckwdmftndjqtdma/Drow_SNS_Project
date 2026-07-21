package com.canvasflow.comment;

import com.canvasflow.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 다른 모듈이 댓글 정보를 읽기 전용으로 조회할 때 쓰는 공개 창구 (post의 PostReader, like의 LikeReader와 같은 역할).
 * feed 모듈의 "내가 댓글 단 글" 목록용으로 추가함 - comment 담당자 확인 부탁드립니다.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CommentReader {

    private final CommentRepository commentRepository;

    /** 이 유저가 댓글을 남긴 게시글 id 목록 (중복 제거, 삭제된 댓글 제외). */
    public List<Long> findCommentedPostIds(Long userId) {
        return commentRepository.findDistinctPostIdsByWriterId(userId);
    }
}
