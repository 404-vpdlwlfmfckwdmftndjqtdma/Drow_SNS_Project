# 게시글 모듈 아키텍처 — 텍스트 블러 템플릿

> 유료 구독 기능(텍스트 블러, 이미지 블러, …)을 **모듈**로 만들어
> 게시글(core)에 꽂는 구조의 설명서. `textblur` 모듈이 구현 템플릿이다.
> 새 모듈 담당자는 이 문서 + `domain/textblur` 폴더를 복사해서 시작하면 된다.

---

## 1. 핵심 아이디어: 콘센트와 플러그

- **core(post 도메인)** 는 "콘센트"(확장 인터페이스)만 정의한다. 블러가 뭔지 모른다.
- **각 모듈**(textblur, imageblur, …)은 그 인터페이스를 구현한 "플러그"다.
- Spring이 `@Component` 붙은 구현체를 전부 찾아 `List<PostModule>`로 core에 주입한다(컬렉션 주입).
- → **모듈을 추가/삭제해도 core 수정 0줄.** (DIP·OCP, 파이프라인 패턴)

```
                ┌─ core (post) ──────────────────────────┐
                │  PostModule 인터페이스 (콘센트)           │
                │  PostService = 조립/파이프라인 실행       │
                └────────▲───────────────▲────────────────┘
                         │ implements    │ implements
                  TextBlurModule   (ImageBlurModule …)
                  자기 테이블:       자기 테이블:
                  text_blur_ranges  (image_blur_…)
```

## 2. 보안 원칙 (제일 중요)

**블러는 서버에서 처리한다.** 비구독자에게는 본문의 블러 구간을 `●`로 치환한
사본만 응답하고, **원문은 브라우저에 아예 보내지 않는다.**
프론트의 CSS 블러는 장식일 뿐이다. F12로도 원문을 볼 수 없는 이유.

원본의 유일한 보관처는 DB이고, 서버가 문지기다.

## 3. 파일 지도

### 백엔드
| 파일 | 소유 | 역할 |
|---|---|---|
| `domain/post/extension/PostModule.java` | core | **확장 인터페이스** (key / processView / saveExtension / loadExtension / onDelete) |
| `domain/post/extension/PostViewContent.java` | core | 조회 파이프라인을 흐르는 "가공 중인 내용물" |
| `domain/post/service/PostService.java` | core | `List<PostModule>` 주입받아 조회/저장 파이프라인 실행 |
| `domain/textblur/TextBlurModule.java` | 모듈 | 구현 템플릿 ★ 새 모듈은 이 파일을 참고 |
| `domain/textblur/entity/TextBlurRange.java` | 모듈 | 모듈 소유 테이블 (posts에 컬럼 추가 금지!) |
| `domain/subscription/.../ContentAccessService.isSubscribedToUser()` | subscription | 모듈들이 쓰는 구독 판정 **공개 API** |

### 프론트
| 파일 | 소유 | 역할 |
|---|---|---|
| `components/modules/textblur/BlurredText.tsx` | 모듈 | 조회 화면 렌더러 (무상태 — 서버 완성본을 그리기만) |
| `components/modules/textblur/useTextBlurEditor.ts` | 모듈 | 작성 화면 상태 훅 (블러 구간 관리 + buildExtension) |
| `app/posts/[id]/page.tsx` | core | 상세: extensions.textBlur가 있으면 모듈 렌더러에 위임 |
| `app/posts/new/page.tsx` | core | 작성: 제출 시 모듈들의 buildExtension()을 걷어 extensions로 조립 |

## 4. 데이터 흐름

### 저장 (작성/수정)
```
프론트: 모듈 상태를 걷어 한 요청으로 조립
  POST /api/v1/posts
  { "title": "...", "content": "우리 회사 연봉은 5200만원 입니다",
    "extensions": { "textBlur": { "ranges": [ { "start": 9, "end": 15 } ] } } }
        │
        ▼
core(PostService): posts 저장 → 각 모듈의 saveExtension(postId, editorId, 자기구역JSON) 호출
        │                          (extensions 내용은 해석하지 않고 전달만)
        ▼
TextBlurModule: 기존 삭제 후 새로 저장 (전체 교체) → text_blur_ranges 테이블
※ 전부 한 @Transactional → 모듈 저장 실패 시 본문 저장까지 롤백
```

### 조회
```
GET /api/v1/posts/17  (X-User-Id: 열람자)
        │
core: DB 원본으로 PostViewContent 생성
        │
        ▼  for (module : modules) module.processView(content, viewerId)
TextBlurModule:
   구독자/작성자 → 원문 유지 + extensions.textBlur = { ranges, unlocked: true }
   비구독자     → 해당 구간 ● 치환 + { ranges, unlocked: false }
        │
        ▼
응답 JSON → 프론트 BlurredText가 조각내어 렌더링 (잠긴 구간 클릭 → 구독 유도)
```

### 수정 화면 (hydration 규칙 ★)
```
GET /api/v1/posts/17/edit   (작성자 본인만, 마스킹 없는 원문 + 모듈 원자료)
→ 프론트가 응답을 수정 화면 상태에 통째로 채움
→ 완료 시 "현재 본문 기준 전체 상태"를 다시 통째로 전송 (부분 전송 금지)
이유: 백엔드가 전체 교체 방식이라, 안 건드린 모듈 구역도 매번 보내야 보존된다.
또 본문이 바뀌면 오프셋이 밀리므로 ranges는 항상 새 본문 기준으로 재계산해서 보낸다.
```

## 5. 새 모듈 만들기 (예: 이미지 블러)

1. `domain/imageblur/` 패키지 생성 — **자기 패키지 밖은 건드리지 않는다**
2. 자기 엔티티/테이블 생성 (`image_blur_…`) — posts/post_media에 컬럼 추가 금지
3. `ImageBlurModule implements PostModule` + `@Component` — key는 `"imageBlur"`
4. 프론트 `components/modules/imageblur/` 에 렌더러/에디터 훅 작성
5. `types/index.ts` 의 `PostExtensions` 에 자기 타입 추가
6. 작성 페이지의 extensions 조립부에 `imageBlur: …buildExtension()` 한 줄 추가

**금지 사항**
- 남의 모듈 패키지 import (모듈끼리 가로 의존 금지)
- 남의 테이블에 쓰기 / core 테이블에 컬럼 추가
- 다른 도메인 데이터가 필요하면 그 도메인의 **공개 서비스 메서드**로만 접근
  (예: 구독 판정은 `ContentAccessService.isSubscribedToUser()`)

## 6. key 문자열 하나로 전부 연결된다

```
"textBlur" =
  프론트 요청/응답 extensions 의 구역 이름
  = 백엔드 PostModule.key()
  = types/index.ts PostExtensions 의 필드명
```
이름 하나만 합의하면 프론트-백엔드-DB가 전부 짝이 맞는다.

## 7. 남은 일 / 확장 아이디어

- [ ] JWT 필터 완성 후 `X-User-Id` 헤더 → 토큰의 사용자 ID로 교체
- [ ] 수정 페이지(`/posts/[id]/edit`)에 hydration 흐름 연결 (백엔드 API는 준비됨)
- [ ] 이미지 블러 모듈 (업로드 시 블러본 생성 → 블러본/원본 URL 교체 방식)
- [ ] Spring Modulith 도입 시 모듈 경계를 빌드 타임에 검증 가능 (`verify()` 테스트)
