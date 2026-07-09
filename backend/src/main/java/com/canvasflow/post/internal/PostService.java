package com.canvasflow.post.internal;

import com.canvasflow.post.PostExtension;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [핵심] 게시글 저장 파이프라인. core 와 기능 모듈이 만나는 유일한 지점.
 *
 * Spring 이 @Component 붙은 PostExtension 구현체 전부를 List 로 주입한다. (컬렉션 주입)
 * → 기능 모듈이 늘어나도 이 클래스는 수정 0줄. (DIP / OCP)
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final List<PostExtension> postExtensions;

    public PostService(PostRepository postRepository, List<PostExtension> postExtensions) {
        this.postRepository = postRepository;
        this.postExtensions = postExtensions;
    }

    @Transactional
    public PostEntity save(PostSaveRequestDto request) {
        // ① 먼저 저장 → postId 확보 (기능 모듈이 post_id 로 자기 테이블에 저장하려면 필요)
        PostEntity post = postRepository.save(new PostEntity(request.text()));

        // ② 각 기능 모듈에게 postId + 자기 칸(section)만 넘긴다. 전체가 한 트랜잭션.
        Map<String, Object> extensions = request.extensions();
        for (PostExtension ext : postExtensions) {
            Object section = (extensions == null) ? null : extensions.get(ext.key());
            ext.apply(post.getId(), section);
        }
        return post;
    }

    /**
     * 테스트용: DB의 모든 게시글을 조회 파이프라인을 통과시켜 반환. (페이징/필터 없음)
     * 각 글의 text 를 기능 모듈들의 render() 에 순서대로 통과시킨다 → 블러 등이 여기서 적용됨.
     */
    @Transactional(readOnly = true)
    public List<PostView> list() {
        return postRepository.findAll().stream()
                .map(post -> {
                    String text = post.getText();
                    for (PostExtension ext : postExtensions) {
                        text = ext.render(post.getId(), text);
                    }
                    return new PostView(post.getId(), text);
                })
                .toList();
    }
}
