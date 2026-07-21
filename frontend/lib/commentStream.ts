const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export interface CommentStreamItem {
  id: number;
  postId: number;
  parentId: number | null;
  writerId: number;
  writerNickname?: string;
  writerProfileImageUrl?: string | null;
  content: string;
  deleted: boolean;
  createdAt: string;
  likeCount: number;
  likedByMe: boolean;
  replies: CommentStreamItem[];
}

export interface CommentDeletedEvent {
  id: number;
  hardDeleted: boolean;
}

export interface CommentLikeCountEvent {
  commentId: number;
  likeCount: number;
}

interface CommentStreamHandlers {
  onCreated: (comment: CommentStreamItem) => void;
  onUpdated: (comment: CommentStreamItem) => void;
  onDeleted: (event: CommentDeletedEvent) => void;
  onLikeCount: (event: CommentLikeCountEvent) => void;
}

// 댓글 읽기는 공개 콘텐츠라 인증 없이 구독한다 (백엔드 CommentController.subscribe 참고).
// 이 게시글(postId)의 댓글을 보고 있는 모든 클라이언트가 생성/수정/삭제/좋아요 이벤트를 실시간으로 받는다.
// 댓글 좋아요 개수도 댓글마다 따로 연결을 열지 않고 이 채널 하나로 같이 받는다 (브라우저 오리진당 동시 연결 수 제한 때문).
export function subscribeToComments(postId: number, handlers: CommentStreamHandlers): () => void {
  const source = new EventSource(`${API_BASE}/api/v1/posts/${postId}/comments/subscribe`);

  source.addEventListener("comment-created", (event) => {
    handlers.onCreated(JSON.parse((event as MessageEvent).data));
  });
  source.addEventListener("comment-updated", (event) => {
    handlers.onUpdated(JSON.parse((event as MessageEvent).data));
  });
  source.addEventListener("comment-deleted", (event) => {
    handlers.onDeleted(JSON.parse((event as MessageEvent).data));
  });
  source.addEventListener("comment-like-count", (event) => {
    handlers.onLikeCount(JSON.parse((event as MessageEvent).data));
  });

  return () => source.close();
}
