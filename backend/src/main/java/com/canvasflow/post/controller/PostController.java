package com.canvasflow.post.controller;

import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.service.PostService;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@RequestHeader("X-User-Id") Long userId,
                                                  @RequestBody PostRequestDto postRequestDto) {
        PostEntity postEntity = postService.createPost(userId, postRequestDto);
        return ResponseEntity.ok(ApiResponse.ok(postEntity.getPostId()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostViewDto>>> getAllPosts() {
        return ResponseEntity.ok(ApiResponse.ok(postService.getAllPosts()));
    }


}
