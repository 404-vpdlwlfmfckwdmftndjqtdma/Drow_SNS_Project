package com.canvasflow.domain.channel.repository;

import com.canvasflow.domain.channel.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {

    Optional<ChannelMember> findByChannelIdAndUserId(Long channelId, Long userId);

    boolean existsByChannelIdAndUserId(Long channelId, Long userId);

    long countByChannelId(Long channelId);
}
