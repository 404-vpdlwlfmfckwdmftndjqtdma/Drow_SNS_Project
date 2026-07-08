package com.canvasflow.domain.post.entity;

import com.canvasflow.global.media.MediaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public PostMedia(String url, MediaType mediaType, int sortOrder) {
        this.url = url;
        this.mediaType = mediaType;
        this.sortOrder = sortOrder;
    }

    void assignPost(Post post) {
        this.post = post;
    }
}
