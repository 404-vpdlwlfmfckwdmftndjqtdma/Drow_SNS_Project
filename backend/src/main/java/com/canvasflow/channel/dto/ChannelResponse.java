package com.canvasflow.channel.dto;

import com.canvasflow.channel.entity.Channel;
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
    public static ChannelResponse from(Channel channel, String ownerNickname, long memberCount) {
        return new ChannelResponse(
                channel.getId(),
                channel.getOwnerId(),
                ownerNickname,
                channel.getName(),
                channel.getDescription(),
                channel.getDefaultVisibility(),
                memberCount
        );
    }
}
