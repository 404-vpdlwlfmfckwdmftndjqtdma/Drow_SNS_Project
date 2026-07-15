import type { CommentDeletedEvent, CommentLikeCountEvent, CommentStreamItem } from "@/lib/commentStream";

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

type LikeCountHandler = (count: number) => void;
type CommentCountHandler = (count: number) => void;

interface PostListeners {
  likeHandlers: Set<LikeCountHandler>;
  commentHandlers: Set<CommentCountHandler>;
  commentCreatedHandlers: Set<(comment: CommentStreamItem) => void>;
  commentUpdatedHandlers: Set<(comment: CommentStreamItem) => void>;
  commentDeletedHandlers: Set<(event: CommentDeletedEvent) => void>;
  commentLikeCountHandlers: Set<(event: CommentLikeCountEvent) => void>;
}

interface PostLikeCountEvent {
  type: "POST_LIKE_COUNT";
  postId: number;
  likeCount: number;
  commentCount: number | null;
}

interface PostCommentCountEvent {
  type: "POST_COMMENT_COUNT";
  postId: number;
  likeCount: number | null;
  commentCount: number;
}

interface PostScopedCommentDeletedEvent extends CommentDeletedEvent {
  postId: number;
}

interface PostScopedCommentLikeCountEvent extends CommentLikeCountEvent {
  postId: number;
}

// 창당 EventSource 1개를 재사용하고, postId별 리스너만 갈아끼운다.
const listenersByPostId = new Map<number, PostListeners>();
let source: EventSource | null = null;

function getOrCreate(postId: number): PostListeners {
  const existing = listenersByPostId.get(postId);
  if (existing) return existing;

  const created: PostListeners = {
    likeHandlers: new Set(),
    commentHandlers: new Set(),
    commentCreatedHandlers: new Set(),
    commentUpdatedHandlers: new Set(),
    commentDeletedHandlers: new Set(),
    commentLikeCountHandlers: new Set(),
  };
  listenersByPostId.set(postId, created);
  return created;
}

function openOrRefreshConnection() {
  if (source) {
    source.close();
    source = null;
  }

  if (listenersByPostId.size === 0) {
    return;
  }

  const ids = [...listenersByPostId.keys()].sort((a, b) => a - b).join(",");
  // 브라우저 연결 수를 줄이기 위해 postIds 전체를 한 번에 구독한다.
  source = new EventSource(`${API_BASE}/api/v1/posts/stream/subscribe?postIds=${ids}`);

  // 서버가 보낸 이벤트를 postId 기준으로 해당 컴포넌트 리스너에만 전달한다.
  source.addEventListener("post-like-count", (event) => {
    const payload = JSON.parse((event as MessageEvent).data) as PostLikeCountEvent;
    const listeners = listenersByPostId.get(payload.postId);
    if (!listeners) return;
    listeners.likeHandlers.forEach((handler) => handler(payload.likeCount));
  });

  source.addEventListener("post-comment-count", (event) => {
    const payload = JSON.parse((event as MessageEvent).data) as PostCommentCountEvent;
    const listeners = listenersByPostId.get(payload.postId);
    if (!listeners) return;
    listeners.commentHandlers.forEach((handler) => handler(payload.commentCount));
  });

  source.addEventListener("comment-created", (event) => {
    const payload = JSON.parse((event as MessageEvent).data) as CommentStreamItem;
    const listeners = listenersByPostId.get(payload.postId);
    if (!listeners) return;
    listeners.commentCreatedHandlers.forEach((handler) => handler(payload));
  });

  source.addEventListener("comment-updated", (event) => {
    const payload = JSON.parse((event as MessageEvent).data) as CommentStreamItem;
    const listeners = listenersByPostId.get(payload.postId);
    if (!listeners) return;
    listeners.commentUpdatedHandlers.forEach((handler) => handler(payload));
  });

  source.addEventListener("comment-deleted", (event) => {
    const payload = JSON.parse((event as MessageEvent).data) as PostScopedCommentDeletedEvent;
    const listeners = listenersByPostId.get(payload.postId);
    if (!listeners) return;
    listeners.commentDeletedHandlers.forEach((handler) => handler(payload));
  });

  source.addEventListener("comment-like-count", (event) => {
    const payload = JSON.parse((event as MessageEvent).data) as PostScopedCommentLikeCountEvent;
    const listeners = listenersByPostId.get(payload.postId);
    if (!listeners) return;
    listeners.commentLikeCountHandlers.forEach((handler) => handler(payload));
  });
}

export function subscribeToPostFeed(
  postId: number,
  handlers: {
    onLikeCount?: LikeCountHandler;
    onCommentCount?: CommentCountHandler;
    onCommentCreated?: (comment: CommentStreamItem) => void;
    onCommentUpdated?: (comment: CommentStreamItem) => void;
    onCommentDeleted?: (event: CommentDeletedEvent) => void;
    onCommentLikeCount?: (event: CommentLikeCountEvent) => void;
  }
): () => void {
  const listeners = getOrCreate(postId);

  if (handlers.onLikeCount) listeners.likeHandlers.add(handlers.onLikeCount);
  if (handlers.onCommentCount) listeners.commentHandlers.add(handlers.onCommentCount);
  if (handlers.onCommentCreated) listeners.commentCreatedHandlers.add(handlers.onCommentCreated);
  if (handlers.onCommentUpdated) listeners.commentUpdatedHandlers.add(handlers.onCommentUpdated);
  if (handlers.onCommentDeleted) listeners.commentDeletedHandlers.add(handlers.onCommentDeleted);
  if (handlers.onCommentLikeCount) listeners.commentLikeCountHandlers.add(handlers.onCommentLikeCount);

  openOrRefreshConnection();

  return () => {
    const current = listenersByPostId.get(postId);
    if (!current) return;

    if (handlers.onLikeCount) current.likeHandlers.delete(handlers.onLikeCount);
    if (handlers.onCommentCount) current.commentHandlers.delete(handlers.onCommentCount);
    if (handlers.onCommentCreated) current.commentCreatedHandlers.delete(handlers.onCommentCreated);
    if (handlers.onCommentUpdated) current.commentUpdatedHandlers.delete(handlers.onCommentUpdated);
    if (handlers.onCommentDeleted) current.commentDeletedHandlers.delete(handlers.onCommentDeleted);
    if (handlers.onCommentLikeCount) current.commentLikeCountHandlers.delete(handlers.onCommentLikeCount);

    if (
      current.likeHandlers.size === 0
      && current.commentHandlers.size === 0
      && current.commentCreatedHandlers.size === 0
      && current.commentUpdatedHandlers.size === 0
      && current.commentDeletedHandlers.size === 0
      && current.commentLikeCountHandlers.size === 0
    ) {
      listenersByPostId.delete(postId);
    }

    // 구독 postId 목록이 바뀌면 연결을 재생성해 서버 필터를 최신화한다.
    openOrRefreshConnection();
  };
}
