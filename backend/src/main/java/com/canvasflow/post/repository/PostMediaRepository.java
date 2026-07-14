package com.canvasflow.post.repository;

import com.canvasflow.post.entity.PostMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMediaEntity, Long> {
    // postId 목록으로 media를 한 번에 조회 (N+1 방지) post별 sortOrder 순으로 정렬해서 반환
    List<PostMediaEntity> findByPostIdInOrderByPostIdAscSortOrderAsc(List<Long> postIds);

    //삭제
    void deleteAllByPostId(Long postId);
}
