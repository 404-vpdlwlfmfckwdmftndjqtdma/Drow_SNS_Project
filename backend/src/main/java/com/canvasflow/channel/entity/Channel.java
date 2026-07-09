package com.canvasflow.channel.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import com.canvasflow.global.common.ContentVisibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주제별 채널. 채널 소유자가 게시글 공개 범위 기본 정책을 설정할 수 있다
 * (개별 게시글에서 override 가능 - Post.visibility 우선 적용).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "channels")
public class Channel extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentVisibility defaultVisibility;

    @Builder
    public Channel(Long ownerId, String name, String description, ContentVisibility defaultVisibility) {
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.defaultVisibility = defaultVisibility == null ? ContentVisibility.PUBLIC : defaultVisibility;
    }

    // TODO: update(name, description, defaultVisibility) 구현
}
