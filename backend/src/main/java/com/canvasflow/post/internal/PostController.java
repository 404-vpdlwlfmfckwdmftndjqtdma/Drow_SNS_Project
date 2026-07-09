package com.canvasflow.post.internal;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public Long save(@RequestBody PostSaveRequestDto request) {
        return postService.save(request).getId();
    }

    /** 테스트용: DB의 모든 게시글 목록 (GET /api/v1/post). text 는 블러 등이 적용된 결과. */
    @GetMapping
    public List<PostView> list() {
        return postService.list();
    }
}
