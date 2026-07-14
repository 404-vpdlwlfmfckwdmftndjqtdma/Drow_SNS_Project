package com.canvasflow.post.entity;

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
public class PostMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long mediaId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column(name = "sort_order")
    private int sortOrder;

    @Builder
    public PostMediaEntity(Long postId, String url, MediaType mediaType, int sortOrder){
        this.postId = postId;
        this.url = url;
        this.mediaType = mediaType;
        this.sortOrder = sortOrder;
    }
}
