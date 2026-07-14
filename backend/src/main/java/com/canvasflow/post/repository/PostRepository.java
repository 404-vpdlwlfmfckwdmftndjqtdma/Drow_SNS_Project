package com.canvasflow.post.repository;

import com.canvasflow.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 목록 조회용: findAll()로 다 가져온 뒤 자바 코드에서 거르지 않고, DB 쿼리 단계에서부터 걸러서 가져온다.
    // (걸러질 글까지 매번 다 퍼올 필요 없음 + getAllPosts()에서 단건용 필터링 코드를 반복 안 써도 됨)
    //
    // 조건 1) p.deletedAt IS NULL           → 삭제(soft delete)된 글은 아예 목록에서 제외
    // 조건 2) 비공개(PRIVATE)가 아니거나, PRIVATE인데 그 글의 주인(userId)이 조회하는 사람(viewerId)과 같으면 통과
    //         → PUBLIC/LOCKED는 항상 통과, PRIVATE는 작성자 본인일 때만 통과
    //
    // viewerId가 null로 들어와도(비로그인 사용자) 에러 안 남: SQL에서 "컬럼 = NULL" 비교는 항상 거짓으로 처리되기 때문에
    // 자동으로 "PRIVATE 글은 전부 제외"가 되고, 따로 null 체크 코드를 안 짜도 된다.
    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.deletedAt IS NULL
          AND (p.visibility <> com.canvasflow.global.common.ContentVisibility.PRIVATE
               OR p.userId = :viewerId)
        ORDER BY p.createdAt DESC
        """)
    List<PostEntity> findVisiblePosts(@Param("viewerId") Long viewerId);


}
