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

    @Query(value = """
            SELECT p.* FROM posts p
            WHERE p.deleted_at IS NULL
                AND (p.visibility <> 'PRIVATE' OR p.user_id = :userId)
                AND EXISTS (
                            SELECT 1
                            FROM likes l
                            WHERE l.user_id = :userId
                                AND l.target_type = 'POST'
                                AND l.target_id = p.post_id
                )
            ORDER BY p.created_at DESC
            """, nativeQuery = true)
    List<PostEntity> findVisiblePostsLikedByUser(@Param("userId") Long userId);

    @Query(value = """
            SELECT p.* FROM posts p
            WHERE p.deleted_at IS NULL
                AND (p.visibility <> 'PRIVATE' OR p.user_id = :userId)
                AND EXISTS (
                            SELECT 1
                            FROM comments c
                            WHERE c.post_id = p.post_id
                                AND c.writer_id = :userId
                                AND c.deleted_at IS NULL
                )
            ORDER BY p.created_at DESC
            """, nativeQuery = true)
    List<PostEntity> findVisiblePostsCommentedByUser(@Param("userId") Long userId);

    // mypage 모듈(PostReader.countByAuthorId)에서 마이페이지 "창작물" 통계용으로 추가함 - post 담당자 확인 부탁드립니다.
    // 삭제(soft delete)된 글은 제외하고 센다. visibility(PRIVATE 등)는 구분하지 않고 전부 포함 - 본인/타인 마이페이지 모두
    // "이 사람이 쓴 글 총개수"라는 의미로 쓰기 위함이다(공개된 글만 셀지는 추후 논의 필요).
    long countByUserIdAndDeletedAtIsNull(Long userId);

    // mypage 모듈(PostReader.getPostsByAuthorId)에서 마이페이지/타인 프로필 포트폴리오 그리드용으로 추가함 - post 담당자 확인 부탁드립니다.
    // countByUserIdAndDeletedAtIsNull과 동일하게 삭제된 글만 제외하고 visibility는 구분하지 않는다
    // (본인 마이페이지에서 비공개 글도 같이 보여줄지는 추후 논의 필요 - 지금은 우선 전부 포함).
    List<PostEntity> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    // mypage 모듈(PostReader.sumViewCountByAuthorId)에서 마이페이지 "조회수" 통계용으로 추가함 - post 담당자 확인 부탁드립니다.
    // 삭제된 글은 합산에서 제외한다. 글이 하나도 없으면 SUM이 NULL을 반환하므로 COALESCE로 0 처리.
    @Query("""
        SELECT COALESCE(SUM(p.viewCount), 0) FROM PostEntity p
        WHERE p.userId = :userId AND p.deletedAt IS NULL
        """)
    long sumViewCountByUserId(@Param("userId") Long userId);
}
