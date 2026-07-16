package com.canvasflow.search;

import com.canvasflow.global.response.ApiResponse;
import com.canvasflow.post.PostReader;
import com.canvasflow.post.PostSearchView;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostSearchView>>> searchPosts(
            @RequestParam("tag") String tag) {
        return ResponseEntity.ok(ApiResponse.<List<PostSearchView>>ok(searchService.searchPostsByTag(tag)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileView>>> searchUsers(
            @RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.searchUsers(nickname)));
    }
}
