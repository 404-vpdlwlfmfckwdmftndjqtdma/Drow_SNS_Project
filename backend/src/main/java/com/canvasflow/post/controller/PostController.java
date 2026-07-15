package com.canvasflow.post.controller;

import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.service.PostService;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //게시글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@RequestHeader("X-User-Id") Long userId,
                                                  @RequestBody PostRequestDto postRequestDto) {
        PostEntity postEntity = postService.createPost(userId, postRequestDto);
        return ResponseEntity.ok(ApiResponse.ok(postEntity.getPostId()));
    }

    // required = false: 비로그인 사용자도 피드는 봐야 하니까 헤더가 없어도 에러 내지 않고 viewerId=null로 받는다
    //게시글 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostViewDto>>> getAllPosts(
            @RequestHeader(value = "X-User-Id", required = false) Long viewerId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam(value = "activity", required = false) String activity) {
        Long resolvedViewerId = authMember != null ? authMember.userId() : viewerId;
        return ResponseEntity.ok(ApiResponse.ok(postService.getAllPosts(resolvedViewerId, activity)));
    }

    //게시글 상세
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostViewDto>> getPost(
            @RequestHeader(value = "X-User-Id", required = false) Long viewerId,
            @PathVariable Long postId){
        return ResponseEntity.ok(ApiResponse.ok(postService.getDetail(viewerId, postId)));
    }

    //게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long postId,
            @RequestBody PostRequestDto postRequestDto){
        postService.updatePost(userId, postId, postRequestDto);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    //게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long postId){
        postService.deletePost(userId, postId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }



}
