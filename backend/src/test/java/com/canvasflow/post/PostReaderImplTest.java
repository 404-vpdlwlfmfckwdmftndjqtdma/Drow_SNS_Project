package com.canvasflow.post;

import com.canvasflow.global.common.ContentVisibility;
import com.canvasflow.global.media.MediaType;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.repository.PostMediaRepository;
import com.canvasflow.post.repository.PostProductRepository;
import com.canvasflow.post.repository.PostRepository;
import com.canvasflow.post.service.PostReaderImpl;
import com.canvasflow.post.service.PostViewAssembler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * post 모듈이 다른 모듈에 글을 내보내는 창구 테스트.
 *
 * ★ 이 테스트의 존재 이유: 예전에 getPostsByAuthorId 가 렌더 파이프라인을 건너뛰고
 *   엔티티 원문을 그대로 내보내서, 프로필/채널 화면으로 블러 원문과 원본 이미지 URL이
 *   유출된 적이 있다. 목록 창구도 반드시 assembler 를 거쳐야 한다는 것을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class PostReaderImplTest {

    private static final Long AUTHOR_ID = 3L;
    private static final Long VIEWER_ID = 42L;
    private static final Long POST_ID = 31L;

    @Mock
    PostRepository postRepository;

    @Mock
    PostMediaRepository postMediaRepository;

    @Mock
    PostProductRepository postProductRepository;

    @Mock
    PostViewAssembler assembler;

    @InjectMocks
    PostReaderImpl postReader;

    @Test
    @DisplayName("포트폴리오 목록은 원문이 아니라 렌더 파이프라인 통과본을 내보낸다")
    void 목록도_렌더_파이프라인을_거친다() {
        PostEntity post = PostEntity.builder()
                .userId(AUTHOR_ID)
                .content("연봉은 5200만원 입니다")   // 원문
                .visibility(ContentVisibility.PUBLIC)
                .tags(List.of())
                .build();
        given(postRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(AUTHOR_ID))
                .willReturn(List.of(post));

        // assembler가 블러 처리한 결과를 돌려주는 상황
        given(assembler.toViewDtos(anyList(), eq(VIEWER_ID))).willReturn(List.of(new PostViewDto(
                AUTHOR_ID,
                POST_ID,
                "연봉은 ●●●●●● 입니다",                                    // 마스킹된 본문
                ContentVisibility.PUBLIC,
                List.of(),
                List.of(new PostRequestDto.MediaItem(
                        "https://cdn/upload/e_blur:3000/a.jpg", MediaType.IMAGE)),  // 블러 URL
                0L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "화가딩",
                null
        )));

        List<PostReader.PostSummary> result = postReader.getPostsByAuthorId(AUTHOR_ID, VIEWER_ID);

        // 열람자 기준 판정을 하려면 viewerId가 반드시 assembler까지 전달돼야 한다
        verify(assembler).toViewDtos(anyList(), eq(VIEWER_ID));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content())
                .isEqualTo("연봉은 ●●●●●● 입니다")
                .doesNotContain("5200");                    // 원문이 새면 실패
        assertThat(result.get(0).thumbnailUrl()).contains("e_blur:3000");   // 원본 URL이 새면 실패
    }

    @Test
    @DisplayName("글이 없으면 조회도 렌더도 하지 않는다")
    void 글이_없으면_빈_목록() {
        given(postRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(AUTHOR_ID))
                .willReturn(List.of());

        assertThat(postReader.getPostsByAuthorId(AUTHOR_ID, VIEWER_ID)).isEmpty();
    }
}
