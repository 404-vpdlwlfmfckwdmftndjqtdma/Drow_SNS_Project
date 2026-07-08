// 공통 TypeScript 타입. 백엔드 DTO(도메인별 dto/*.java)와 1:1로 맞춰 관리한다.
// 필드 수정 전 팀원 공지.

export type ContentVisibility = "PUBLIC" | "BLUR" | "BLACKBOX" | "RESTRICTED" | "PARTIAL";

export type MediaType = "IMAGE" | "VIDEO";

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 현재 페이지(0-base)
  size: number;
}

export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl?: string;
  bio?: string;
}

export interface PostMedia {
  url: string;
  mediaType: MediaType;
  sortOrder: number;
}

export interface PostSummary {
  id: number;
  title: string;
  thumbnailUrl?: string;
  authorId: number;
  authorNickname: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  locked: boolean;
  createdAt: string;
}

export interface PostDetail {
  id: number;
  authorId: number;
  authorNickname: string;
  channelId?: number;
  title: string;
  content: string | null; // locked=true 면 null
  visibility: ContentVisibility;
  locked: boolean;
  viewCount: number;
  tags: string[];
  mediaUrls: string[];
  createdAt: string;
}

// 수정 화면 hydration용 — GET /posts/{id}/edit 응답 (작성/수정 스토어에 통째로 채움)
export interface PostEditData {
  title: string;
  content: string | null;
  visibility: ContentVisibility;
  tags?: string[];
  /** 모듈별 확장 데이터. key = 백엔드 PostModule.key() */
  extensions: Record<string, unknown>;
}

export interface Channel {
  id: number;
  ownerId: number;
  ownerNickname: string;
  name: string;
  description?: string;
  defaultVisibility: ContentVisibility;
  memberCount: number;
}

export interface Comment {
  id: number;
  postId: number;
  authorId: number;
  authorNickname: string;
  content: string;
  createdAt: string;
}

export type SubscriptionTargetType = "USER" | "CHANNEL";

export interface Subscription {
  id: number;
  targetType: SubscriptionTargetType;
  targetId: number;
  tier: string;
  status: "ACTIVE" | "CANCELED";
}

export type NotificationType = "COMMENT" | "LIKE" | "NEW_FOLLOWER" | "NEW_POST" | "NEW_SUBSCRIBER";

export interface AppNotification {
  id: number;
  type: NotificationType;
  message: string;
  relatedId?: number;
  isRead: boolean;
  createdAt: string;
}

export interface PostSearchCondition {
  keyword?: string;
  channelId?: number;
  contentType?: string;
  tag?: string;
  sort?: "LATEST" | "LIKES" | "COMMENTS" | "VIEWS";
}

// textblur 모듈 — 본문 블러 구간 [start, end) (백엔드 TextBlurRange 와 1:1)
export interface BlurRange {
  start: number;
  end: number;
}
