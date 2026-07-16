package com.canvasflow.imageblur.internal;

import jakarta.persistence.*;

@Entity
@Table(name = "image_blur_targets", indexes = @Index(name = "idx_ibt_post", columnList = "postId"))
public class ImageBlurTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private int mediaIndex;

    protected ImageBlurTarget(){}

    public ImageBlurTarget(Long postId, int mediaIndex){
        this.postId = postId;
        this.mediaIndex = mediaIndex;
    }

    public Long getId(){
        return id;
    }

    public Long getPostId(){
        return postId;
    }

    public int getMediaIndex(){
        return mediaIndex;
    }
}
