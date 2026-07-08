package com.canvasflow.domain.post.extension;

/**
 * [게시글 확장 모듈 인터페이스 - core(post) 소유]
 *
 * 게시글에 기능을 얹는 모듈(텍스트 블러, 이미지 블러 …)이 구현하는 계약.
 * 구현체에 @Component 만 붙이면 Spring 이 List<PostModule> 로 전부 주입해 주므로
 * core 는 어떤 모듈이 몇 개 있는지 몰라도 된다. (DIP / OCP)
 *
 * 딱 두 지점에 끼어든다:
 *   - 저장 시   → saveExtension() : 자기 구역 데이터를 자기 테이블에 저장
 *   - 상세 조회 시 → processView()  : 비구독자면 내용을 가공(블러 등)
 *
 * key() 문자열 하나(예: "textBlur")가 프론트 extensions 구역 이름 = 이 모듈 식별자다.
 */
public interface PostModule {

    /** extensions JSON 에서 이 모듈이 소유하는 구역 이름. 프론트 모듈 key 와 반드시 일치. */
    String key();

    /**
     * [저장 파이프라인] 게시글 저장 시, 요청 extensions[key] 데이터를 받아 자기 테이블에 저장.
     * data 는 Jackson 이 역직렬화한 순수 Java 객체(JSON object→Map, array→List, 숫자→Integer/Long).
     * data 가 null 이면 "이 모듈 데이터 없음".
     */
    void saveExtension(Long postId, Long authorId, Object data);

    /**
     * [조회 파이프라인] 게시글이 열람자에게 나가기 직전 내용 가공 기회.
     * 비구독자면 여기서 마스킹(●)하고, 원본은 절대 응답에 싣지 않는다. (보안은 서버에서 종료)
     */
    void processView(PostViewContent content, Long viewerId);
}
