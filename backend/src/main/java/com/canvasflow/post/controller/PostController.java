package com.canvasflow.post.controller;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.global.security.AuthMember;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.service.PostService;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    //게시글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@AuthenticationPrincipal AuthMember authMember,
                                                        @RequestBody PostRequestDto postRequestDto) {
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        PostEntity postEntity = postService.createPost(authMember.userId(), postRequestDto);
        return ResponseEntity.ok(ApiResponse.ok(postEntity.getPostId()));
    }

    //게시글 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostViewDto>>> getAllPosts(
            @AuthenticationPrincipal AuthMember authMember) {
        Long viewerId = authMember != null ? authMember.userId() : null;
        return ResponseEntity.ok(ApiResponse.ok(postService.getAllPosts(viewerId)));
    }

    //게시글 상세
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostViewDto>> getPost(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId){
        Long viewerId = authMember != null ? authMember.userId() : null;
        return ResponseEntity.ok(ApiResponse.ok(postService.getDetail(viewerId, postId)));
    }

    //게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId,
            @RequestBody PostRequestDto postRequestDto){
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        postService.updatePost(authMember.userId(), postId, postRequestDto);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    //게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long postId){
        if (authMember == null) {
            throw new CanvasflowException(ErrorCode.UNAUTHORIZED);
        }
        postService.deletePost(authMember.userId(), postId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }



}
