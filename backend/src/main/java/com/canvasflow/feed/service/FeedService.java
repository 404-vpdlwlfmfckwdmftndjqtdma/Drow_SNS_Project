package com.canvasflow.feed.service;

import com.canvasflow.comment.CommentReader;
import com.canvasflow.follow.FollowFacade;
import com.canvasflow.follow.dto.FollowUserResponse;
import com.canvasflow.like.LikeReader;
import com.canvasflow.post.PostReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 담당: 마이페이지 "내 활동" 피드 3종 - 팔로우한 사람들 글 / 내가 좋아요한 글 / 내가 댓글 단 글.
 * follow/like/comment/post 모듈의 테이블·리포지토리를 직접 참조하지 않고, 각 모듈이 공개해 둔
 * 읽기 창구(FollowFacade, LikeReader, CommentReader, PostReader)만 조합한다.
 */
@RequiredArgsConstructor
@Service
public class FeedService {

    private final FollowFacade followFacade;
    private final LikeReader likeReader;
    private final CommentReader commentReader;
    private final PostReader postReader;

    public List<PostReader.PostView> getFollowingFeed(Long userId) {
        List<Long> followingIds = followFacade.getFollowingList(userId).stream()
                .map(FollowUserResponse::userId)
                .toList();
        return postReader.getPostsByAuthorIds(followingIds, userId);
    }

    public List<PostReader.PostView> getMyLikedPosts(Long userId) {
        List<Long> postIds = likeReader.findLikedPostIds(userId);
        return postReader.getViewablePosts(postIds, userId);
    }

    public List<PostReader.PostView> getMyCommentedPosts(Long userId) {
        List<Long> postIds = commentReader.findCommentedPostIds(userId);
        return postReader.getViewablePosts(postIds, userId);
    }
}
