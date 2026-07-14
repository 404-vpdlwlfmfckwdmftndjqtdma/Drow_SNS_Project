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
        LocalDateTime updatedAt

//       ,
        //TODO 좋아요 개수와 댓글 개수, 닉네임 나중에 연결
//        int likeCount,
//        int commentCount,
//        String nickname

        //TODO 유저의 닉네임 요청
        /* 이렇게 만들어 달라 요청하면 될 것 같다
        String getNickname(Long userId);        //상세조회용
        Map<Long, String getNicknames(List<Long> userIds);      //목록조회용
         */

        ) {
}
