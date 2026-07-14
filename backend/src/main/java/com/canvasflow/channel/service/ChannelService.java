package com.canvasflow.channel.service;

import com.canvasflow.channel.dto.ChannelCreateRequest;
import com.canvasflow.channel.dto.ChannelResponse;
import com.canvasflow.channel.entity.Channel;
import com.canvasflow.channel.entity.ChannelMember;
import com.canvasflow.channel.repository.ChannelMemberRepository;
import com.canvasflow.channel.repository.ChannelRepository;
import com.canvasflow.user.service.UserService;
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
    private final UserService userService;

    @Transactional
    public Long create(Long ownerId, ChannelCreateRequest request) {
        if (!userService.existsById(ownerId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        Channel channel = Channel.builder()
                .ownerId(ownerId)
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
        String ownerNickname = userService.findNicknameById(channel.getOwnerId());
        return ChannelResponse.from(channel, ownerNickname, memberCount);
    }

    @Transactional
    public void addMember(Long channelId, Long userId) {
        if (channelMemberRepository.existsByChannelIdAndUserId(channelId, userId)) {
            return; // 이미 추가됨 - idempotent
        }
        Channel channel = getChannelOrThrow(channelId);
        if (!userService.existsById(userId)) {
            throw new CanvasflowException(ErrorCode.USER_NOT_FOUND);
        }
        channelMemberRepository.save(ChannelMember.builder().channel(channel).userId(userId).build());
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
