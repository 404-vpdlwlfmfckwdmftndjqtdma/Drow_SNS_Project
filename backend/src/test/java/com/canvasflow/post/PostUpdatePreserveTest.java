package com.canvasflow.post;

import com.canvasflow.global.common.ContentVisibility;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostProductRepository;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.post.service.PostService;
import com.canvasflow.post.service.PostViewAssembler;
import com.canvasflow.user.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * 글 수정 시 블러/가격표 보존 테스트.
 *
 * ★ 이 테스트의 존재 이유: 예전에는 요청에 extensions가 없으면 "전부 삭제"로 처리했다.
 *   그런데 수정 화면은 extensions를 보내지 않기 때문에, 글 내용만 고쳐도 블러가 통째로
 *   사라졌다. 가격표(prices)도 같은 함정에 빠질 뻔했다.
 *   규칙: 안 보낸 항목(null)은 손대지 않는다. 지우려면 빈 값을 명시적으로 보낸다.
 */
@ExtendWith(MockitoExtension.class)
class PostUpdatePreserveTest {

    private static final Long AUTHOR_ID = 3L;
    private static final Long POST_ID = 31L;

    @Mock
    PostRepository postRepository;

    @Mock
    PostMediaRepository postMediaRepository;

    @Mock
    PostProductRepository postProductRepository;

    @Mock
    UserFacade userFacade;

    @Mock
    PostViewAssembler postViewAssembler;

    /**
     * 확장 모듈이 호출됐는지 보려고 스파이를 하나 끼운다.
     * PostService는 List<PostExtension>을 컬렉션 주입으로 받는데 @InjectMocks가 이걸 못 채우므로,
     * 서비스는 직접 조립한다.
     */
    RecordingExtension extension;
    PostService postService;

    @BeforeEach
    void setUp() {
        extension = spy(new RecordingExtension());
        postService = new PostService(
                postRepository, postMediaRepository, postProductRepository,
                userFacade, List.of(extension), postViewAssembler);
    }

    /** apply 호출 여부만 기록하는 테스트용 확장 모듈 */
    static class RecordingExtension implements PostExtension {
        final List<Object> applied = new ArrayList<>();

        @Override
        public String key() {
            return "textBlur";
        }

        @Override
        public void apply(Long postId, Object section) {
            applied.add(section);
        }
    }

    private PostEntity livePost() {
        PostEntity post = PostEntity.builder()
                .userId(AUTHOR_ID)
                .content("연봉은 5200만원 입니다")
                .visibility(ContentVisibility.PUBLIC)
                .tags(List.of())
                .build();
        ReflectionTestUtils.setField(post, "postId", POST_ID);
        return post;
    }

    private PostRequestDto request(Map<String, Object> extensions, Map<String, BigDecimal> prices) {
        return new PostRequestDto("수정된 본문", ContentVisibility.PUBLIC, List.of(), List.of(), extensions, prices);
    }

    @Test
    @DisplayName("extensions·prices를 안 보내면 기존 블러와 가격표를 건드리지 않는다")
    void 안_보내면_보존된다() {
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(livePost()));

        postService.updatePost(AUTHOR_ID, POST_ID, request(null, null));

        // 확장 모듈이 호출조차 되면 안 된다 (호출되면 그 안에서 기존 구간을 지운다)
        verify(extension, never()).apply(anyLong(), any());
        // 가격표도 손대지 않아야 한다
        verify(postProductRepository, never()).deleteAllByPostId(anyLong());
    }

    @Test
    @DisplayName("빈 값을 명시적으로 보내면 기존 블러와 가격표가 지워진다")
    void 빈_값을_보내면_삭제된다() {
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(livePost()));

        postService.updatePost(AUTHOR_ID, POST_ID, request(Map.of(), Map.of()));

        // 모듈에 null 구역이 전달되어 "이번엔 없음" = 기존 것 삭제로 처리된다
        verify(extension).apply(eq(POST_ID), isNull());
        verify(postProductRepository).deleteAllByPostId(POST_ID);
    }

    @Test
    @DisplayName("가격을 보내면 가격표가 교체된다")
    void 가격을_보내면_교체된다() {
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(livePost()));

        postService.updatePost(AUTHOR_ID, POST_ID,
                request(null, Map.of("textBlur", BigDecimal.valueOf(1000))));

        verify(postProductRepository).deleteAllByPostId(POST_ID);
        verify(postProductRepository).saveAll(any());
        // extensions는 안 보냈으므로 블러는 그대로 둬야 한다
        verify(extension, never()).apply(anyLong(), any());
    }
}
