package com.canvasflow.post.repository;

import com.canvasflow.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 목록 조회용: findAll()로 다 가져온 뒤 자바 코드에서 거르지 않고, DB 쿼리 단계에서부터 걸러서 가져온다.
    // 삭제(soft delete)된 글만 제외 - PRIVATE(나만보기)는 더 이상 쓰지 않아 별도 필터링 없음.
    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
        """)
    List<PostEntity> findVisiblePosts();

    // PostReader.getViewablePosts용: id 목록으로 삭제 안 된 글만 한 방에 조회한다.
    // IN 쿼리는 순서를 보장하지 않으므로 입력 id 순서 보정은 호출부(PostReaderImpl)에서 한다.
    List<PostEntity> findByPostIdInAndDeletedAtIsNull(List<Long> postIds);

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

    // 검색 페이지 "태그 검색"용: p.tags는 PostEntity가 직접 소유한 컬렉션(post_tags 테이블, @ElementCollection)이라
    // 다른 모듈 테이블을 참조하지 않는다. 부분/대소문자 무관 일치, 같은 글이 키워드를 여러 태그에 포함해도
    // 한 번만 나오도록 DISTINCT.
    @Query("""
        SELECT DISTINCT p FROM PostEntity p JOIN p.tags t
        WHERE p.deletedAt IS NULL AND LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.createdAt DESC
        """)
    List<PostEntity> findByTagContaining(@Param("keyword") String keyword);

    // PostReader.getPostsByAuthorIds용 (follow 모듈의 "팔로잉 피드"): 여러 작성자의 글을 최신순으로 한 번에 조회.
    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.userId IN :userIds AND p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
        """)
    List<PostEntity> findByUserIdInAndDeletedAtIsNull(@Param("userIds") List<Long> userIds);
}
