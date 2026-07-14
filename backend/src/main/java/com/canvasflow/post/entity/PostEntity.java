package com.canvasflow.post.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import com.canvasflow.global.common.ContentVisibility;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class PostEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Lob
    @Column(length = 800)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentVisibility visibility;   //PUBLIC, PRIVATE, LOCKED

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Builder
    public PostEntity(Long userId, String content, ContentVisibility visibility, List<String> tags) {
        this.userId = userId;
        this.content = content;
        this.visibility = visibility == null ? ContentVisibility.PUBLIC : visibility;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.viewCount = 0L;
    }

    public void update(String content, ContentVisibility visibility, List<String> tags){
        this.content = content;
        this.visibility = visibility == null ? ContentVisibility.PUBLIC : visibility;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public void delete(){
        this.deletedAt = LocalDateTime.now();
    }
}
