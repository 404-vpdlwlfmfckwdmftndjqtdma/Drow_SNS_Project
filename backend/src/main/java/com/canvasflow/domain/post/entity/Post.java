package com.canvasflow.domain.post.entity;

import com.canvasflow.domain.channel.entity.Channel;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.global.common.BaseTimeEntity;
import com.canvasflow.global.common.ContentVisibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 그림/글/영상을 자유롭게 혼합 첨부할 수 있는 게시글.
 * 실제 파일 목록은 PostMedia 로 별도 관리(1:N).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 채널 소속 게시글이 아닐 수도 있으므로 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentVisibility visibility;

    @Column(nullable = false)
    private long viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostMedia> mediaList = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Builder
    public Post(User author, Channel channel, String title, String content, ContentVisibility visibility, List<String> tags) {
        this.author = author;
        this.channel = channel;
        this.title = title;
        this.content = content;
        this.visibility = visibility == null ? ContentVisibility.PUBLIC : visibility;
        this.viewCount = 0;
        if (tags != null) {
            this.tags = tags;
        }
    }

    public void addMedia(PostMedia media) {
        mediaList.add(media);
        media.assignPost(this);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    // TODO: update(title, content, visibility, tags) 메서드로 수정 로직 구현
    public void update(String title, String content, ContentVisibility visibility) {
        this.title = title;
        this.content = content;
        this.visibility = visibility;
    }
}
