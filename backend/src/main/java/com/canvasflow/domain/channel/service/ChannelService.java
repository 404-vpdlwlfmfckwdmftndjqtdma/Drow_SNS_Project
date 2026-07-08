package com.canvasflow.domain.channel.service;

import com.canvasflow.domain.channel.dto.ChannelCreateRequest;
import com.canvasflow.domain.channel.dto.ChannelResponse;
import com.canvasflow.domain.channel.entity.Channel;
import com.canvasflow.domain.channel.entity.ChannelMember;
import com.canvasflow.domain.channel.repository.ChannelMemberRepository;
import com.canvasflow.domain.channel.repository.ChannelRepository;
import com.canvasflow.domain.user.entity.User;
import com.canvasflow.domain.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long ownerId, ChannelCreateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
        Channel channel = Channel.builder()
                .owner(owner)
                .name(request.name())
                .description(request.description())
                .defaultVisibility(request.defaultVisibility())
                .build();
        return channelRepository.save(channel).getId();
    }

    @Transactional(readOnly = true)
    public ChannelResponse getDetail(Long channelId) {
        Channel channel = getChannelOrThrow(channelId);
        long memberCount = channelMemberRepository.countByChannelId(channelId);
        return ChannelResponse.from(channel, memberCount);
    }

    @Transactional
    public void addMember(Long channelId, Long userId) {
        if (channelMemberRepository.existsByChannelIdAndUserId(channelId, userId)) {
            return; // 이미 추가됨 - idempotent
        }
        Channel channel = getChannelOrThrow(channelId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
        channelMemberRepository.save(ChannelMember.builder().channel(channel).user(user).build());
    }

    @Transactional
    public void removeMember(Long channelId, Long userId) {
        channelMemberRepository.findByChannelIdAndUserId(channelId, userId)
                .ifPresent(channelMemberRepository::delete);
    }

    private Channel getChannelOrThrow(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.CHANNEL_NOT_FOUND));
    }
}
