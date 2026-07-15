"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import { type CommentDeletedEvent, type CommentLikeCountEvent } from "@/lib/commentStream";
import { subscribeToPostFeed } from "@/lib/postFeedStream";
import CommentRow, { type CommentActions, type CommentItem } from "./CommentRow";
import styles from "./CommentModal.module.css";

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
}

interface PageEnvelope<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface CommentModalProps {
  postId: number;
  userId: number | null;
  onClose: () => void;
}

// [comment 모듈 - 팝업 콘텐츠] CommentButton 이 열어주는 실제 댓글 CRUD UI.
// 생성/수정/삭제는 이 게시글을 보고 있는 모두에게 SSE로 실시간 브로드캐스트된다 (내 화면도 이 채널로만 갱신됨).
export default function CommentModal({ postId, userId, onClose }: CommentModalProps) {
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [rootContent, setRootContent] = useState("");
  const [replyDrafts, setReplyDrafts] = useState<Record<number, string>>({});
  const [editDrafts, setEditDrafts] = useState<Record<number, string>>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchComments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postId]);

  useEffect(() => {
    return subscribeToPostFeed(postId, {
      onCommentCreated: (comment) => setComments((prev) => insertComment(prev, comment)),
      onCommentUpdated: (comment) => setComments((prev) => mergeUpdated(prev, comment)),
      onCommentDeleted: (event) => setComments((prev) => applyDeleted(prev, event)),
      onCommentLikeCount: (event) => setComments((prev) => applyLike(prev, event.commentId, undefined, event.likeCount)),
    });
  }, [postId]);

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  async function fetchComments() {
    setLoading(true);
    try {
      const res = await api.get<ApiEnvelope<PageEnvelope<CommentItem>>>(
        `/api/v1/posts/${postId}/comments`,
        { params: { size: 50 } }
      );
      setComments(res.data.data.content);
    } catch {
      alert("댓글을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }

  // 생성/수정/삭제는 서버가 SSE로 브로드캐스트해주므로 여기서 직접 상태를 갱신하지 않는다 (성공 시 각자 fetch 필요 없음).
  async function createComment(content: string, parentId?: number) {
    if (!content.trim()) return;
    if (userId == null) {
      alert("댓글 작성은 로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      await api.post(`/api/v1/posts/${postId}/comments`, { content, parentId });
    } catch {
      alert("댓글 등록에 실패했습니다.");
    }
  }

  async function updateComment(id: number, content: string) {
    if (!content.trim()) return;
    if (userId == null) {
      alert("댓글 수정은 로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      await api.put(`/api/v1/comments/${id}`, { content });
    } catch {
      alert("댓글 수정에 실패했습니다.");
    }
  }

  async function deleteComment(id: number) {
    if (userId == null) {
      alert("댓글 삭제는 로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      await api.delete(`/api/v1/comments/${id}`);
    } catch {
      alert("댓글 삭제에 실패했습니다.");
    }
  }

  async function toggleLike(c: CommentItem) {
    if (userId == null) {
      alert("좋아요는 로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      const res = await api.request<ApiEnvelope<{ liked: boolean; likeCount: number }>>({
        method: c.likedByMe ? "delete" : "post",
        url: `/api/v1/likes/COMMENT/${c.id}`,
      });
      const { liked, likeCount } = res.data.data;
      setComments((prev) => applyLike(prev, c.id, liked, likeCount));
    } catch {
      alert("좋아요 처리에 실패했습니다.");
    }
  }

  const actions: CommentActions = {
    replyDrafts,
    editDrafts,
    onToggleLike: toggleLike,
    onStartReply: (id) => setReplyDrafts((prev) => ({ ...prev, [id]: prev[id] ?? "" })),
    onReplyChange: (id, value) => setReplyDrafts((prev) => ({ ...prev, [id]: value })),
    onSubmitReply: (id) => {
      const content = replyDrafts[id] ?? "";
      createComment(content, id).then(() =>
        setReplyDrafts((prev) => {
          const next = { ...prev };
          delete next[id];
          return next;
        })
      );
    },
    onStartEdit: (c) => setEditDrafts((prev) => ({ ...prev, [c.id]: c.content })),
    onEditChange: (id, value) => setEditDrafts((prev) => ({ ...prev, [id]: value })),
    onSubmitEdit: (id) => {
      const content = editDrafts[id] ?? "";
      updateComment(id, content).then(() =>
        setEditDrafts((prev) => {
          const next = { ...prev };
          delete next[id];
          return next;
        })
      );
    },
    onCancelEdit: (id) =>
      setEditDrafts((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      }),
    onDelete: deleteComment,
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.panel} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <span className={styles.title}>댓글</span>
          <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <div className={styles.body}>
          <div className={styles.row}>
            <input
              className={styles.input}
              placeholder={userId == null ? "로그인 후 댓글을 작성할 수 있습니다" : "댓글을 입력하세요"}
              value={rootContent}
              onChange={(e) => setRootContent(e.target.value)}
              disabled={userId == null}
            />
            <button
              className={styles.submit}
              disabled={userId == null}
              onClick={() => {
                createComment(rootContent);
                setRootContent("");
              }}
            >
              등록
            </button>
          </div>

          {loading ? (
            <p className={styles.empty}>불러오는 중...</p>
          ) : comments.length === 0 ? (
            <p className={styles.empty}>아직 댓글이 없습니다.</p>
          ) : (
            comments.map((c) => (
              <CommentRow key={c.id} comment={c} depth={0} currentUserId={userId} actions={actions} />
            ))
          )}
        </div>
      </div>
    </div>
  );
}

function insertComment(list: CommentItem[], comment: CommentItem): CommentItem[] {
  if (comment.parentId == null) {
    return list.some((c) => c.id === comment.id) ? list : [...list, comment];
  }
  return list.map((c) =>
    c.id === comment.parentId
      ? { ...c, replies: c.replies.some((r) => r.id === comment.id) ? c.replies : [...c.replies, comment] }
      : c
  );
}

function mergeUpdated(list: CommentItem[], updated: CommentItem): CommentItem[] {
  return list.map((c) =>
    c.id === updated.id
      ? {
          ...c,
          content: updated.content,
          deleted: updated.deleted,
          likeCount: updated.likeCount,
          likedByMe: updated.likedByMe,
          writerNickname: updated.writerNickname,
        }
      : { ...c, replies: mergeUpdated(c.replies, updated) }
  );
}

function applyDeleted(list: CommentItem[], event: CommentDeletedEvent): CommentItem[] {
  if (event.hardDeleted) {
    return list
      .filter((c) => c.id !== event.id)
      .map((c) => ({ ...c, replies: c.replies.filter((r) => r.id !== event.id) }));
  }
  return list.map((c) =>
    c.id === event.id ? { ...c, deleted: true } : { ...c, replies: applyDeleted(c.replies, event) }
  );
}

// likedByMe가 undefined면(다른 사람 좋아요로 인한 개수 브로드캐스트) 기존 값을 유지하고 likeCount만 갱신한다.
function applyLike(list: CommentItem[], id: number, likedByMe: boolean | undefined, likeCount: number): CommentItem[] {
  return list.map((c) =>
    c.id === id
      ? { ...c, likedByMe: likedByMe ?? c.likedByMe, likeCount }
      : { ...c, replies: applyLike(c.replies, id, likedByMe, likeCount) }
  );
}
