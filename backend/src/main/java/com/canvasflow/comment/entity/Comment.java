package com.canvasflow.comment.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * post_id/parent_id/writer_id는 다른 모듈(post/user) 소유 데이터의 ID만 저장하며 FK를 걸지 않는다
 * (무결성은 애플리케이션 레벨에서 보장). parent_id는 1-depth까지만 허용(대댓글의 대댓글 금지)하며,
 * 이 검증은 서비스 레이어에서 수행한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comments")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Comment(Long postId, Long parentId, Long writerId, String content) {
        this.postId = postId;
        this.parentId = parentId;
        this.writerId = writerId;
        this.content = content;
        this.status = CommentStatus.ACTIVE; // 생성 시점엔 항상 정상 상태
    }

    // parentId 유무로 원댓글/대댓글을 구분. 이 값이 true인 댓글에는 새 parentId로 다시 못 달게
    // 서비스 레이어에서 막아서 1-depth를 유지한다 (대댓글의 대댓글 금지).
    public boolean isReply() {
        return parentId != null;
    }

    public boolean isDeleted() {
        return status == CommentStatus.DELETED;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    // 자식(대댓글)이 남아있는 댓글을 지울 때 호출. row를 유지해야 자식이 부모를 잃지 않는다.
    // 자식이 없는 댓글은 이 메서드 대신 repository.delete()로 하드삭제한다 (CommentService 참고).
    public void softDelete() {
        this.status = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
