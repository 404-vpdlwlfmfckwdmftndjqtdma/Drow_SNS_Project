package com.canvasflow.post;

import com.canvasflow.global.common.ContentVisibility;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostProductRepository;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.post.service.PostService;
import com.canvasflow.post.service.PostViewAssembler;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostViewCountTest {

    private static final Long AUTHOR_ID = 3L;
    private static final Long VIEWER_ID = 7L;
    private static final Long POST_ID = 31L;

    @Mock PostRepository postRepository;
    @Mock PostMediaRepository postMediaRepository;
    @Mock PostProductRepository postProductRepository;
    @Mock UserFacade userFacade;
    @Mock PostViewAssembler postViewAssembler;

    private PostService postService;
    private PostEntity post;

    @BeforeEach
    void setUp() {
        postService = new PostService(
                postRepository, postMediaRepository, postProductRepository,
                userFacade, List.of(), postViewAssembler, Optional.empty());

        post = PostEntity.builder()
                .userId(AUTHOR_ID)
                .content("본문")
                .visibility(ContentVisibility.PUBLIC)
                .tags(List.of())
                .build();
        ReflectionTestUtils.setField(post, "postId", POST_ID);
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
    }

    @Test
    @DisplayName("상세 GET은 조회수를 변경하지 않는다")
    void detailReadDoesNotIncreaseViewCount() {
        given(postMediaRepository.findByPostIdInOrderByPostIdAscSortOrderAsc(List.of(POST_ID)))
                .willReturn(List.of());
        given(userFacade.findNicknameById(AUTHOR_ID)).willReturn("작성자");
        given(userFacade.getProfileView(AUTHOR_ID))
                .willReturn(new UserProfileView(AUTHOR_ID, "작성자", null, null));

        postService.getDetail(VIEWER_ID, POST_ID);

        assertEquals(0L, post.getViewCount());
    }

    @Test
    @DisplayName("다른 사용자의 최초 열람 기록은 조회수를 증가시킨다")
    void visitorViewIncreasesViewCount() {
        postService.recordView(VIEWER_ID, POST_ID);

        assertEquals(1L, post.getViewCount());
    }

    @Test
    @DisplayName("작성자 본인의 열람은 조회수에 포함하지 않는다")
    void ownerViewDoesNotIncreaseViewCount() {
        postService.recordView(AUTHOR_ID, POST_ID);

        assertEquals(0L, post.getViewCount());
    }
}
