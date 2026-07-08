package com.canvasflow.domain.post.dto;

import com.canvasflow.global.common.ContentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank @Size(max = 200) String title,
        String content,
        Long channelId,
        ContentVisibility visibility,
        List<String> tags,
        List<PostMediaRequest> mediaList
) {
    public record PostMediaRequest(String url, String mediaType, int sortOrder) {
    }
}
