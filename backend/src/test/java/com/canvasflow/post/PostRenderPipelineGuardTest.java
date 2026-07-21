package com.canvasflow.post;

import com.canvasflow.post.entity.PostEntity;
import com.canvasflow.post.entity.PostMediaEntity;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * [렌더 파이프라인 강제 장치]
 *
 * 게시글 원문과 원본 미디어 URL은 반드시 렌더 파이프라인(PostViewContent → PostViewAssembler)을
 * 거쳐서만 밖으로 나가야 한다. 여기를 우회하면 비구독자에게 원문이 유출된다.
 *
 * 실제로 PostReaderImpl.getPostsByAuthorId 가 파이프라인을 건너뛰고 엔티티 원문을 그대로
 * 내보내, 프로필/채널 화면으로 블러 원문과 원본 이미지 URL이 새는 사고가 있었다.
 * 그때는 컴파일도 되고 테스트도 통과했다 - 규칙이 "약속"이었을 뿐 강제가 아니었기 때문이다.
 *
 * 이 테스트는 그 약속을 빌드 실패로 강제한다. 아직 존재하지 않는 코드까지 막아주므로,
 * 새 조회 메서드를 추가하며 같은 실수를 하면 여기서 걸린다.
 *
 * (허용 클래스는 이름 문자열로 지정한다 - PostViewContent 는 post 모듈 내부 전용이라
 *  package-private 이고, 다른 패키지인 이 테스트에서 타입으로 참조할 수 없다.)
 */
class PostRenderPipelineGuardTest {

    /** 카트에 원문을 싣는 지점. 여기서 읽은 원문은 곧바로 렌더 단계로 넘어간다. */
    private static final String POST_VIEW_CONTENT = "com.canvasflow.post.service.PostViewContent";

    /** 렌더 파이프라인 본체 (블러 적용). */
    private static final String POST_VIEW_ASSEMBLER = "com.canvasflow.post.service.PostViewAssembler";

    /** 상세 조회에서 미디어를 카트에 싣는 지점. 직후 renderForViewer 를 호출한다. */
    private static final String POST_SERVICE = "com.canvasflow.post.service.PostService";

    private static JavaClasses canvasflow;

    @BeforeAll
    static void importClasses() {
        canvasflow = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.canvasflow");
    }

    @Test
    @DisplayName("게시글 원문(content)은 렌더 파이프라인 안에서만 읽을 수 있다")
    void 원문은_파이프라인만_읽는다() {
        ArchRule rule = noClasses()
                .that().doNotHaveFullyQualifiedName(POST_VIEW_CONTENT)
                .should().callMethod(PostEntity.class, "getContent")
                .because("""
                        엔티티 원문을 직접 읽어 응답에 담으면 블러가 적용되지 않아 원문이 유출된다.
                        글을 밖으로 내보내는 코드는 PostViewAssembler(toViewDtos / renderForViewer)를 거칠 것.
                        """);

        rule.check(canvasflow);
    }

    @Test
    @DisplayName("원본 미디어 URL은 렌더 파이프라인 안에서만 읽을 수 있다")
    void 원본_URL은_파이프라인만_읽는다() {
        ArchRule rule = noClasses()
                .that().doNotHaveFullyQualifiedName(POST_VIEW_ASSEMBLER)
                .and().doNotHaveFullyQualifiedName(POST_SERVICE)
                .should().callMethod(PostMediaEntity.class, "getUrl")
                .because("""
                        원본 URL을 그대로 내보내면 이미지 블러가 적용되지 않는다.
                        읽은 URL은 반드시 렌더 파이프라인(renderMedia)을 통과시킨 뒤 응답에 담을 것.
                        """);

        rule.check(canvasflow);
    }
}
