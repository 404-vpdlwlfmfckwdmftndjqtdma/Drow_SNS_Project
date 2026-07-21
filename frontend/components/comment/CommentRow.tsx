"use client";

import Link from "next/link";
import CommentAvatar, { UnknownAvatar } from "./CommentAvatar";
import styles from "./CommentModal.module.css";
import { formatRelativeTime } from "./dateTime";

export interface CommentItem {
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
  const displayName = c.deleted ? "알 수 없음" : c.writerNickname ?? `유저 ${c.writerId}`;
  const createdAtLabel = formatRelativeTime(c.createdAt);
  const avatarSize = depth === 0 ? 36 : 28;

  return (
    <div className={depth === 0 ? styles.commentRow : styles.replyRow}>
      {c.deleted ? (
        <UnknownAvatar size={avatarSize} />
      ) : (
        <CommentAvatar userId={c.writerId} nickname={displayName} profileImageUrl={c.writerProfileImageUrl} size={avatarSize} />
      )}

      <div className={styles.commentBody}>
        <div className={styles.commentHeader}>
          {c.deleted ? (
            <span className={styles.nicknameUnknown}>{displayName}</span>
          ) : (
            <Link href={`/users/${c.writerId}`} className={isMine ? styles.nicknameMine : styles.nickname}>
              {displayName}
            </Link>
          )}
          <span className={styles.timestamp}>{createdAtLabel}</span>
        </div>

        {editDraft === undefined ? (
          <p className={c.deleted ? styles.contentDeleted : styles.content}>
            {c.deleted ? "삭제된 댓글입니다" : c.content}
          </p>
        ) : (
          <div className={styles.editRow}>
            <input
              className={styles.input}
              value={editDraft}
              onChange={(e) => actions.onEditChange(c.id, e.target.value)}
              autoFocus
            />
            <button className={styles.smallSubmit} onClick={() => actions.onSubmitEdit(c.id)}>
              저장
            </button>
            <button className={styles.smallCancel} onClick={() => actions.onCancelEdit(c.id)}>
              취소
            </button>
          </div>
        )}

        <div className={styles.actionsRow}>
          {!c.deleted && (
            <button className={styles.likeBtn} onClick={() => actions.onToggleLike(c)}>
              <span className={`material-symbols-outlined${c.likedByMe ? " filled" : ""}`} style={{ fontSize: 16 }}>
                favorite
              </span>
              {c.likeCount}
            </button>
          )}
          {depth === 0 && (
            <button className={styles.actionBtn} onClick={() => actions.onStartReply(c.id)}>
              답글
            </button>
          )}
          {isMine && !c.deleted && editDraft === undefined && (
            <button className={styles.actionBtn} onClick={() => actions.onStartEdit(c)}>
              수정
            </button>
          )}
          {isMine && !c.deleted && (
            <button className={styles.actionBtn} onClick={() => actions.onDelete(c.id)}>
              삭제
            </button>
          )}
        </div>

        {c.id in actions.replyDrafts && depth === 0 && (
          <div className={styles.editRow}>
            <input
              className={styles.input}
              placeholder="대댓글 입력"
              value={replyDraft}
              onChange={(e) => actions.onReplyChange(c.id, e.target.value)}
              autoFocus
            />
            <button className={styles.smallSubmit} onClick={() => actions.onSubmitReply(c.id)}>
              등록
            </button>
          </div>
        )}

        {c.replies?.map((r) => (
          <CommentRow key={r.id} comment={r} depth={depth + 1} currentUserId={currentUserId} actions={actions} />
        ))}
      </div>
    </div>
  );
}
