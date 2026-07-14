package com.canvasflow.post.service;

import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import com.canvasflow.post.dto.PostRequestDto;
import com.canvasflow.post.dto.PostViewDto;
import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * [핵심] 게시글 저장 파이프라인. core 와 기능 모듈이 만나는 유일한 지점.
 *
 * Spring 이 @Component 붙은 PostExtension 구현체 전부를 List 로 주입한다. (컬렉션 주입)
 * → 기능 모듈이 늘어나도 이 클래스는 수정 0줄. (DIP / OCP)
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;


    @Transactional
    public PostEntity createPost(Long userId, PostRequestDto postRequestDto){
        //TODO 임시로 내용이 없으면 에러 처리(미디어 추가 후 수정하기)
        if(postRequestDto.content() == null || postRequestDto.content().isBlank()){
            throw new CanvasflowException(ErrorCode.POST_CONTENT_REQUIRED);
        }

        PostEntity postEntity = postRepository.save(new PostEntity(userId, postRequestDto.content(), postRequestDto.visibility(), postRequestDto.tags()));

        return postEntity;
    }

    @Transactional(readOnly = true)
    public List<PostViewDto> getAllPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(post -> new PostViewDto(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getVisibility(),
                        List.copyOf(post.getTags()),
                        List.of(),  //TODO media 자리
                        post.getViewCount(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();
    }





}
