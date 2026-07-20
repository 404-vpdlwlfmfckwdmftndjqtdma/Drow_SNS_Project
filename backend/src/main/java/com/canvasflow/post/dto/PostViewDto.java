package com.canvasflow.post.dto;

// 가공 후의 데이터 dto

import com.canvasflow.global.common.ContentVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record PostViewDto(
        Long userId,
        Long postId,
        String content,
        ContentVisibility visibility,
        List<String> tags,
        List<PostRequestDto.MediaItem> media,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String nickname,
        String profileImageUrl  //프로필 사진 조회


        ) {
}
