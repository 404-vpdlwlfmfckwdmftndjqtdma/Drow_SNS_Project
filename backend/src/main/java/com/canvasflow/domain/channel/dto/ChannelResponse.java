package com.canvasflow.domain.channel.dto;

import com.canvasflow.domain.channel.entity.Channel;
import com.canvasflow.global.common.ContentVisibility;

public record ChannelResponse(
        Long id,
        Long ownerId,
        String ownerNickname,
        String name,
        String description,
        ContentVisibility defaultVisibility,
        long memberCount
) {
    public static ChannelResponse from(Channel channel, long memberCount) {
        return new ChannelResponse(
                channel.getId(),
                channel.getOwner().getId(),
                channel.getOwner().getNickname(),
                channel.getName(),
                channel.getDescription(),
                channel.getDefaultVisibility(),
                memberCount
        );
    }
}
