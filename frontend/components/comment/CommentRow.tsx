"use client";

import styles from "./CommentModal.module.css";
import { formatCommentDateTime } from "./dateTime";

export interface CommentItem {
  id: number;
  postId: number;
  parentId: number | null;
  writerId: number;
  writerNickname?: string;
  content: string;
  deleted: boolean;
  createdAt: string;
  likeCount: number;
  likedByMe: boolean;
  replies: CommentItem[];
}

export interface CommentActions {
  replyDrafts: Record<number, string>;
  editDrafts: Record<number, string>;
  onToggleLike: (c: CommentItem) => void;
  onStartReply: (id: number) => void;
  onReplyChange: (id: number, value: string) => void;
  onSubmitReply: (id: number) => void;
  onStartEdit: (c: CommentItem) => void;
  onEditChange: (id: number, value: string) => void;
  onSubmitEdit: (id: number) => void;
  onCancelEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

interface CommentRowProps {
  comment: CommentItem;
  depth: number;
  currentUserId: number | null;
  actions: CommentActions;
}

// 댓글 한 줄(+ 대댓글) 렌더링. 좋아요 개수 실시간 갱신은 댓글마다 SSE 연결을 열지 않고
// CommentModal이 이미 구독 중인 게시글 댓글 채널(comment-like-count 이벤트)로 받아 내려온다
// (브라우저는 오리진당 동시 연결 수 제한이 있어서 댓글 수만큼 연결을 열면 안 된다).
export default function CommentRow({ comment: c, depth, currentUserId, actions }: CommentRowProps) {
  const replyDraft = actions.replyDrafts[c.id] ?? "";
  const editDraft = actions.editDrafts[c.id];
  const isMine = currentUserId != null && c.writerId === currentUserId;
  const displayName = c.writerNickname ?? `유저 ${c.writerId}`;
  const createdAtLabel = formatCommentDateTime(c.createdAt);

  return (
    <div className={styles.comment} style={{ marginLeft: depth * 24 }}>
      <div className={styles.meta}>
        <span className={isMine ? styles.nicknameMine : styles.nickname}>{displayName}</span> · {c.deleted ? "[삭제됨]" : createdAtLabel}
      </div>

      {editDraft === undefined ? (
        <p className={styles.content}>{c.deleted ? "삭제된 댓글입니다" : c.content}</p>
      ) : (
        <div className={styles.row}>
          <input
            className={styles.input}
            value={editDraft}
            onChange={(e) => actions.onEditChange(c.id, e.target.value)}
          />
          <button className={styles.submit} onClick={() => actions.onSubmitEdit(c.id)}>
            저장
          </button>
          <button onClick={() => actions.onCancelEdit(c.id)}>취소</button>
        </div>
      )}

      <div className={styles.actions}>
        {!c.deleted && (
          <button className={styles.likeBtn} onClick={() => actions.onToggleLike(c)}>
            <span className={`material-symbols-outlined${c.likedByMe ? " filled" : ""}`} style={{ fontSize: 16 }}>
              favorite
            </span>
            {c.likeCount}
          </button>
        )}
        {depth === 0 && <button onClick={() => actions.onStartReply(c.id)}>답글</button>}
        {isMine && !c.deleted && editDraft === undefined && (
          <button onClick={() => actions.onStartEdit(c)}>수정</button>
        )}
        {isMine && !c.deleted && <button onClick={() => actions.onDelete(c.id)}>삭제</button>}
      </div>

      {c.id in actions.replyDrafts && depth === 0 && (
        <div className={styles.row}>
          <input
            className={styles.input}
            placeholder="대댓글 입력"
            value={replyDraft}
            onChange={(e) => actions.onReplyChange(c.id, e.target.value)}
          />
          <button className={styles.submit} onClick={() => actions.onSubmitReply(c.id)}>
            등록
          </button>
        </div>
      )}

      {c.replies?.map((r) => (
        <CommentRow key={r.id} comment={r} depth={depth + 1} currentUserId={currentUserId} actions={actions} />
      ))}
    </div>
  );
}
