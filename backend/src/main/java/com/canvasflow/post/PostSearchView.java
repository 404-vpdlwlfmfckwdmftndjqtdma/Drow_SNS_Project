package com.canvasflow.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostSearchView(
        Long postId,
        Long userId,
        String nickname,
        String content,
        List<String> tags,
        List<PostSearchView.MediaItem> media,
        LocalDateTime createdAt
) {
    public record MediaItem(String url, String mediaType) {}
}
