package com.canvasflow.comment.repository;

import com.canvasflow.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 원댓글 페이징 조회 (parent_id IS NULL), 오래된 순
    Page<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);

    // 한 페이지에 포함된 원댓글들의 대댓글을 한 번에 조회 (N+1 방지), 오래된 순
    List<Comment> findByParentIdInOrderByCreatedAtAsc(List<Long> parentIds);

    // 삭제 시 하드/소프트 삭제 판단용 (자식 존재 여부)
    boolean existsByParentId(Long parentId);

    long countByPostId(Long postId);

    // 마이페이지 "내가 댓글 단 글" 목록용: 삭제 안 된 댓글 기준으로 이 유저가 댓글을 남긴 postId 목록 (중복 제거).
    @Query("SELECT DISTINCT c.postId FROM Comment c WHERE c.writerId = :writerId AND c.deletedAt IS NULL")
    List<Long> findDistinctPostIdsByWriterId(@Param("writerId") Long writerId);
}
