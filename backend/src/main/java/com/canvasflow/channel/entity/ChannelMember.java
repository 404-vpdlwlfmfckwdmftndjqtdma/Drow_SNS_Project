package com.canvasflow.channel.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 채널을 "추가"(구독 없이 무료로 채널 피드에 편입)한 관계.
 * 유료/등급형 구독은 domain.subscription.entity.Subscription 에서 별도로 관리.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "channel_members", uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "user_id"}))
public class ChannelMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    public ChannelMember(Channel channel, Long userId) {
        this.channel = channel;
        this.userId = userId;
    }
}
