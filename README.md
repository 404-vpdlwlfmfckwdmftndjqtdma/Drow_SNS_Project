# 🎨 CanvasFlow

> 그림·글·영상을 자유롭게 게시하고, 팔로우/채널로 소통하며, 구독 등급에 따라 콘텐츠 공개 범위를 다르게 보여주는 자유형 SNS

> Spring Boot 4.1 + Next.js 16 + PostgreSQL + Cloudinary

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Backend** | Spring Boot 4.1.0, Java 25, Gradle 9.6.1 |
| **Frontend** | Next.js 16 (App Router), React 19, TypeScript |
| **Database** | PostgreSQL 17 |
| **인증** | Spring Security + JWT (MVP는 JWT만, 소셜로그인은 2차 확장) |
| **이미지/영상** | Cloudinary (업로드·변환, 영상 최대 100MB) |

---

## ⚡ 시작하기

### 1단계 — 환경변수 파일 준비

`.env.example` 을 복사해 프로젝트 루트에 `.env` 를 만든다.

```
canvasFlow/
├── .env          ← 여기에 생성 (.env.example 참고)
├── backend/
└── frontend/
```

### 2단계 — DB 실행 (로컬)

```bash
docker compose up -d   # PostgreSQL 컨테이너 실행 (docker-compose.yml)
```

### 3단계 — 백엔드 실행

```bash
cd backend
# gradle wrapper 최초 1회 생성 (로컬에 Gradle 9.6+ 설치되어 있어야 함)
gradle wrapper --gradle-version 9.6.1
./gradlew bootRun
# → http://localhost:8080
```

`spring-dotenv` 라이브러리가 루트의 `.env` 를 자동으로 읽는다. 별도 설정 불필요.
> IntelliJ 사용 시: Run Configuration의 Working Directory를 `canvasFlow/backend` 로 설정.

### 4단계 — 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

프론트 환경변수는 `.env.local` 사용 (`.env.local.example` 참고).

---

## 🔑 환경변수 목록

| 변수명 | 설명 | 사용 위치 |
|--------|------|-----------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | PostgreSQL 접속 정보 | Backend |
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL 계정 | Backend |
| `JWT_SECRET` | JWT 서명 키 | Backend |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary Cloud Name | Backend |
| `CLOUDINARY_API_KEY` | Cloudinary API Key | Backend |
| `CLOUDINARY_API_SECRET` | Cloudinary API Secret | Backend |
| `NEXT_PUBLIC_API_URL` | 백엔드 API URL | Frontend |

---

## 🛠 개발 가이드

### 패키지 구조: 도메인 기준

기능별로 나누기 쉽도록 `domain/{도메인명}` 아래에 controller / dto / entity / repository / service 를 함께 둔다 (계층형 패키지 대신 도메인형 패키지 채택). 각자 담당 도메인 폴더 안에서만 작업하고, 다른 도메인 파일을 고쳐야 하면 담당자와 먼저 협의한다.

```
1. entity/     — 테이블 매핑 (필드는 뼈대만 작성됨, 필요시 추가)
2. dto/        — 요청/응답 record (뼈대 작성됨)
3. repository/ — 필요한 쿼리 메서드 추가
4. service/    — 비즈니스 로직 구현 (TODO 주석 따라)
5. controller/ — 엔드포인트 연결 (뼈대 작성됨)
```

**공통 응답 형식** — 반드시 `ApiResponse<T>` 로 감싸서 반환

```java
return ResponseEntity.ok(ApiResponse.ok(data));
return ResponseEntity.ok(ApiResponse.ok("메시지", data));
throw new CanvasflowException(ErrorCode.POST_NOT_FOUND); // 실패는 GlobalExceptionHandler 가 처리
```

**인증 임시 처리** — JWT 필터(`global/security/JwtAuthenticationFilter`) 완성 전까지는 `@RequestHeader("X-User-Id") Long userId` 로 로그인 사용자를 임시 수신한다. 필터 완성 후 `@AuthenticationPrincipal CustomUserDetails` 로 교체.

**미디어 업로드** — 프론트가 먼저 `POST /api/v1/media/upload(/batch)` 로 Cloudinary 업로드 → URL을 받아 게시글/프로필 요청 본문에 담아 전송. 백엔드는 URL만 저장한다.

### 프론트엔드 개발 순서

```
1. types/index.ts 의 타입 확인 (백엔드 DTO와 1:1 대응, 뼈대 작성됨)
2. lib/api.ts 의 axios 인스턴스 사용 (토큰 자동 첨부)
3. lib/image.ts 로 미디어 업로드 후 URL만 폼에 담기
4. 페이지(app/**)/컴포넌트(components/**) 구현
```

### 개발 모드 / 운영 모드

```yaml
# application.yml
spring:
  profiles:
    active: dev   # 개발 중 — SecurityConfig 가 대부분의 API를 permitAll 처리
                  # JWT 필터 완성 후 prod 로 전환, SecurityConfig 의 anyRequest().permitAll() 주석 처리 해제 필요
```

---

## 📁 프로젝트 구조

```
canvasFlow/
├── .env                                  ← 환경변수 (git 제외)
├── .env.example
├── docker-compose.yml                    ← 로컬 PostgreSQL
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/main/java/com/canvasflow/
│       ├── CanvasflowApplication.java
│       ├── domain/
│       │   ├── auth/            # 회원가입 / 로그인 / 로그아웃 / 토큰 재발급
│       │   ├── user/            # 회원정보, 닉네임·프로필 수정, 마이페이지 요약
│       │   ├── post/            # 게시글 CRUD, 그림/글/영상 혼합 첨부, 조회수, 검색 쿼리
│       │   ├── follow/          # 팔로우 / 언팔로우
│       │   ├── channel/         # 채널 개설 / 채널 추가(무료 구독)
│       │   ├── subscription/    # 구독 신청/해지, 등급, 콘텐츠 잠금 판정(ContentAccessService)
│       │   ├── comment/         # 댓글 CRUD
│       │   ├── like/            # 게시글/댓글 좋아요 (중복 방지)
│       │   ├── notification/    # 알림 저장/조회/읽음 처리
│       │   └── search/          # 검색 진입점 (post 도메인 쿼리 재사용)
│       │       └── {각 도메인}/controller, dto, entity, repository, service
│       └── global/
│           ├── config/          # Security, CORS, JPA, Cloudinary
│           ├── security/        # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetails
│           ├── exception/       # CanvasflowException, ErrorCode, GlobalExceptionHandler
│           ├── response/        # ApiResponse<T>
│           ├── media/           # MediaService/MediaController (Cloudinary 업로드 공용)
│           └── common/          # BaseTimeEntity, ContentVisibility(공개범위 enum)
│
└── frontend/
    ├── app/
    │   ├── (auth)/login, register
    │   ├── posts/[id]/edit, posts/new, posts/[id]
    │   ├── channels/[id], channels/new
    │   ├── users/[id]
    │   ├── mypage/profile, mypage/posts, mypage/follow, mypage/subscriptions
    │   ├── notifications/
    │   └── search/
    ├── components/
    │   ├── layout/       # Header, BottomNav
    │   ├── common/       # Button 등 재사용 컴포넌트
    │   ├── post/         # PostCard, MediaUploader
    │   ├── channel/      # ChannelCard
    │   ├── subscription/ # ContentGate (블러/블랙박스/접근제한/부분공개 렌더링)
    │   ├── comment/       # CommentList, CommentForm
    │   ├── notification/ # NotificationList
    │   └── mypage/        # ProfileEditForm
    ├── lib/
    │   ├── api.ts         # Axios 인스턴스 (토큰 자동 첨부)
    │   ├── auth.ts        # 토큰 저장/삭제
    │   ├── image.ts       # Cloudinary 업로드 (서버 경유)
    │   └── constants.ts
    └── types/index.ts     # 공통 TypeScript 타입 (수정 전 팀원 공지)
```

---

## 📡 API 엔드포인트 목록 (MVP)

| 도메인 | Method | Path |
|--------|--------|------|
| Media | POST | `/api/v1/media/upload` |
| Media | POST | `/api/v1/media/upload/batch` |
| Auth | POST | `/api/v1/auth/signup` |
| Auth | POST | `/api/v1/auth/login` |
| Auth | POST | `/api/v1/auth/reissue` |
| Auth | POST | `/api/v1/auth/logout` |
| User | GET | `/api/v1/users/me` |
| User | PATCH | `/api/v1/users/me/nickname` |
| User | PATCH | `/api/v1/users/me/profile-image` |
| User | GET | `/api/v1/users/{id}` |
| MyPage | GET | `/api/v1/mypage` |
| Post | GET/POST | `/api/v1/posts` |
| Post | GET/PUT/DELETE | `/api/v1/posts/{id}` |
| Follow | POST/DELETE | `/api/v1/follows/{targetUserId}` |
| Channel | POST | `/api/v1/channels` |
| Channel | GET | `/api/v1/channels/{id}` |
| Channel | POST/DELETE | `/api/v1/channels/{id}/members` |
| Subscription | POST | `/api/v1/subscriptions` |
| Subscription | DELETE | `/api/v1/subscriptions/{id}` |
| Comment | POST/GET | `/api/v1/posts/{postId}/comments` |
| Comment | PUT/DELETE | `/api/v1/comments/{id}` |
| Like | POST/DELETE | `/api/v1/likes/{targetType}/{targetId}` |
| Notification | GET | `/api/v1/notifications` |
| Notification | PATCH | `/api/v1/notifications/{id}/read` |
| Search | GET | `/api/v1/search` |

---

## 🔗 도메인 간 연동 포인트

| 연동 | 내용 |
|------|------|
| post ↔ subscription | 게시글 상세 조회 시 `ContentAccessService.isLocked()` 로 구독 여부 판정 후 `PostResponse` 마스킹 |
| post/comment/follow/subscription → notification | 각 액션 발생 시 `NotificationService.notify()` 호출 (현재 TODO 로 표시됨) |
| post ↔ channel | 게시글은 채널에 소속될 수 있음(`Post.channel`, nullable) |
| auth → 전체 | JWT 필터 완성 후 `SecurityContextHolder` 에서 현재 유저 조회, `X-User-Id` 헤더 방식 제거 |

---

## ❓ 참고 — 이번 스캐폴드에서 잡은 설계 결정

- **DB**: PostgreSQL (로컬은 `docker-compose.yml`)
- **미디어 저장**: Cloudinary (백엔드가 업로드를 대행, 프론트는 URL만 다룸)
- **인증 범위**: JWT만 (소셜로그인은 2차 확장 예정 — `[MVP 제외]` 항목)
- **패키지 구조**: 도메인 기준 (`domain/{기능}/controller,dto,entity,repository,service`) — 담당자가 늘어나도 폴더 단위로 작업 범위가 분리되도록 구성
- **공개범위 4종**은 `global/common/ContentVisibility` enum 하나로 Post/Channel이 공유하고, 실제 락 판정은 `subscription` 도메인의 `ContentAccessService` 가 전담

모든 서비스/컨트롤러는 컴파일 가능한 뼈대 + TODO 주석 상태이며, 실제 비즈니스 로직(본인 검증, 알림 연동, QueryDSL 동적 검색, JWT 검증 등)은 구현이 필요하다.
