package com.canvasflow.domain.post.dto;

import com.canvasflow.domain.post.entity.Post;
import com.canvasflow.global.common.ContentVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        Long authorId,
        String authorNickname,
        Long channelId,
        String title,
        String content,
        ContentVisibility visibility,
        boolean locked,
        long viewCount,
        List<String> tags,
        List<String> mediaUrls,
        LocalDateTime createdAt
) {
    // TODO: 구독 여부(locked)에 따라 content/mediaUrls 를 마스킹하는 로직은
    //       ContentAccessService(subscription 도메인)에서 조립하여 채워 넣는다.
    public static PostResponse from(Post post, boolean locked) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getChannel() != null ? post.getChannel().getId() : null,
                post.getTitle(),
                locked ? null : post.getContent(),
                post.getVisibility(),
                locked,
                post.getViewCount(),
                post.getTags(),
                post.getMediaList().stream().map(m -> m.getUrl()).toList(),
                post.getCreatedAt()
        );
    }
}
