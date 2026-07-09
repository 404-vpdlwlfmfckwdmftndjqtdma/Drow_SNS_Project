package com.canvasflow.post.dto;

// 가공 후의 데이터 dto

import com.canvasflow.global.common.ContentVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record PostView(
        Long userId,
        Long postId,
        String content,
        ContentVisibility visibility,
        List<String> tags,
        List<PostRequestDto.MediaItem> media,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

//       ,
        //TODO 좋아요 개수와 댓글 개수는 나중에 연결
//        int likeCount,
//        int commentCount

        ) {
}
