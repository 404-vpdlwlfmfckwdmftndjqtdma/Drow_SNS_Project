package com.canvasflow.domain.post.repository;

import com.canvasflow.domain.post.dto.PostSearchCondition;
import com.canvasflow.domain.post.dto.PostSummaryResponse;
import com.canvasflow.domain.post.entity.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PostRepositoryCustom 의 기본(비-QueryDSL) 구현체.
 * Spring Data JPA 규칙상 클래스명은 반드시 "{RepositoryInterface}Impl" (PostRepositoryImpl) 이어야
 * PostRepository 가 이 구현을 자동으로 사용한다.
 *
 * TODO: 프로젝트에 QueryDSL 을 도입하면 JPAQueryFactory 기반으로 교체 권장
 *       (동적 조건이 늘어날수록 문자열 JPQL 조립보다 타입-세이프한 QueryDSL이 유리).
 * TODO: likeCount / commentCount 는 현재 0으로 고정되어 있음 - Like/Comment 테이블 집계 서브쿼리로 교체 필요.
 * TODO: sort=LIKES, sort=COMMENTS 는 아직 viewCount/createdAt 기준으로 대체됨 - 집계 후 정렬 기준 추가 필요.
 */
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final EntityManager em;

    public PostRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<PostSummaryResponse> search(PostSearchCondition condition, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhereClause(condition, params);
        String orderBy = buildOrderByClause(condition);

        TypedQuery<Post> query = em.createQuery(
                "select p from Post p " + where + orderBy, Post.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Post> posts = query.getResultList();

        TypedQuery<Long> countQuery = em.createQuery(
                "select count(p) from Post p " + where, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        List<PostSummaryResponse> content = posts.stream()
                .map(p -> new PostSummaryResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getMediaList().isEmpty() ? null : p.getMediaList().get(0).getUrl(),
                        p.getAuthor().getId(),
                        p.getAuthor().getNickname(),
                        p.getViewCount(),
                        0L, // TODO: 좋아요 수 집계
                        0L, // TODO: 댓글 수 집계
                        false, // TODO: 조회자 기준 잠금 여부 (ContentAccessService 연동)
                        p.getCreatedAt()
                ))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildWhereClause(PostSearchCondition condition, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("where 1=1");

        if (StringUtils.hasText(condition.keyword())) {
            sb.append(" and (p.title like :keyword or p.content like :keyword)");
            params.put("keyword", "%" + condition.keyword() + "%");
        }
        if (condition.channelId() != null) {
            sb.append(" and p.channel.id = :channelId");
            params.put("channelId", condition.channelId());
        }
        if (StringUtils.hasText(condition.tag())) {
            sb.append(" and :tag member of p.tags");
            params.put("tag", condition.tag());
        }
        if (StringUtils.hasText(condition.contentType())) {
            switch (condition.contentType().toUpperCase()) {
                case "TEXT" -> sb.append(" and p.mediaList is empty");
                case "IMAGE" -> sb.append(" and exists (select 1 from PostMedia m where m.post = p and m.mediaType = com.canvasflow.global.media.MediaType.IMAGE)");
                case "VIDEO" -> sb.append(" and exists (select 1 from PostMedia m where m.post = p and m.mediaType = com.canvasflow.global.media.MediaType.VIDEO)");
                case "MIXED" -> sb.append(" and exists (select 1 from PostMedia m where m.post = p and m.mediaType = com.canvasflow.global.media.MediaType.IMAGE)")
                        .append(" and exists (select 1 from PostMedia m2 where m2.post = p and m2.mediaType = com.canvasflow.global.media.MediaType.VIDEO)");
                default -> { /* 알 수 없는 값은 무시 */ }
            }
        }
        return sb.toString();
    }

    private String buildOrderByClause(PostSearchCondition condition) {
        String sort = condition.sort() == null ? "LATEST" : condition.sort().toUpperCase();
        return switch (sort) {
            case "VIEWS" -> " order by p.viewCount desc";
            // TODO: LIKES/COMMENTS는 집계 서브쿼리 정렬로 교체 필요. 우선 최신순으로 대체.
            case "LIKES", "COMMENTS", "LATEST" -> " order by p.createdAt desc";
            default -> " order by p.createdAt desc";
        };
    }
}
