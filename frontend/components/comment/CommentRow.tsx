"use client";

import type React from "react";
import Link from "next/link";
import styles from "./CommentModal.module.css";
import { formatCommentDateTime } from "./dateTime";

export interface CommentItem {
  id: number;
  postId: number;
  parentId: number | null;
  writerId: number;
  writerNickname?: string;
  writerProfileImageUrl?: string;
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

const AVATAR_PALETTES = [
  { bg: "#ede8fb", color: "#6b38d4" },
  { bg: "#e6edf9", color: "#0058be" },
  { bg: "#fff3e0", color: "#994100" },
  { bg: "#e1f5ee", color: "#0f6e56" },
  { bg: "#fbeaf0", color: "#993556" },
];

function getAvatarStyle(writerId: number) {
  return AVATAR_PALETTES[writerId % AVATAR_PALETTES.length];
}

function getInitials(nickname?: string, writerId?: number): string {
  if (nickname && nickname.length >= 2) return nickname.slice(0, 2);
  if (nickname) return nickname[0];
  return `U${writerId ?? "?"}`;
}

function Avatar({ profileImageUrl, initials, style, className }: {
  profileImageUrl?: string;
  initials: string;
  style: React.CSSProperties;
  className: string;
}) {
  if (profileImageUrl) {
    return (
      <img
        src={profileImageUrl}
        alt={initials}
        className={className}
        style={{ ...style, objectFit: "cover" }}
      />
    );
  }
  return <div className={className} style={style}>{initials}</div>;
}

export default function CommentRow({ comment: c, depth, currentUserId, actions }: CommentRowProps) {
  const replyDraft = actions.replyDrafts[c.id] ?? "";
  const editDraft = actions.editDrafts[c.id];
  const isMine = currentUserId != null && c.writerId === currentUserId;
  const displayName = c.writerNickname ?? `유저 ${c.writerId}`;
  const createdAtLabel = formatCommentDateTime(c.createdAt);
  const avatarStyle = getAvatarStyle(c.writerId);
  const initials = getInitials(c.writerNickname, c.writerId);
  const profileHref = isMine ? "/mypage" : `/users/${c.writerId}`;

  if (depth > 0) {
    return (
      <div className={styles.replyRow}>
        <svg className={styles.replyConnector} width="22" height="36" aria-hidden="true">
          <line x1="10" y1="0" x2="10" y2="22" stroke="#d0d0d0" strokeWidth="1.5" />
          <line x1="10" y1="22" x2="22" y2="22" stroke="#d0d0d0" strokeWidth="1.5" />
        </svg>
        <div className={styles.replyContent}>
          <Link href={profileHref}>
            <Avatar
              profileImageUrl={c.writerProfileImageUrl}
              initials={initials}
              className={styles.replyAvatar}
              style={{ background: avatarStyle.bg, color: avatarStyle.color }}
            />
          </Link>
          <div className={styles.replyBody}>
            <div className={styles.replyMeta}>
              <Link href={profileHref} className={isMine ? styles.replyNicknameMine : styles.replyNickname}>
                {displayName}
              </Link>
              <span className={styles.replyTimestamp}>{createdAtLabel}</span>
            </div>

            {editDraft === undefined ? (
              <p className={styles.replyText}>{c.deleted ? "삭제된 댓글입니다." : c.content}</p>
            ) : (
              <div className={styles.editRow}>
                <input
                  className={styles.input}
                  value={editDraft}
                  onChange={(e) => actions.onEditChange(c.id, e.target.value)}
                />
                <button className={styles.submit} onClick={() => actions.onSubmitEdit(c.id)}>
                  <span className="material-symbols-outlined" style={{ fontSize: 16 }}>check</span>
                </button>
                <button className={styles.cancelBtn} onClick={() => actions.onCancelEdit(c.id)}>취소</button>
              </div>
            )}

            {!c.deleted && (
              <div className={styles.actions}>
                <button
                  className={`${styles.likeBtn}${c.likedByMe ? ` ${styles.liked}` : ""}`}
                  onClick={() => actions.onToggleLike(c)}
                >
                  <span className={`material-symbols-outlined${c.likedByMe ? " filled" : ""}`} style={{ fontSize: 13 }}>
                    favorite
                  </span>
                  {c.likeCount}
                </button>
                {isMine && editDraft === undefined && (
                  <button onClick={() => actions.onStartEdit(c)}>수정</button>
                )}
                {isMine && <button onClick={() => actions.onDelete(c.id)}>삭제</button>}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.comment}>
      <Link href={profileHref}>
        <Avatar
          profileImageUrl={c.writerProfileImageUrl}
          initials={initials}
          className={styles.avatar}
          style={{ background: avatarStyle.bg, color: avatarStyle.color }}
        />
      </Link>

      <div className={styles.commentBody}>
        <div className={styles.meta}>
          <Link href={profileHref} className={isMine ? styles.nicknameMine : styles.nickname}>{displayName}</Link>
          <span className={styles.timestamp}>{c.deleted ? "" : createdAtLabel}</span>
        </div>

        {editDraft === undefined ? (
          <p className={c.deleted ? styles.contentDeleted : styles.content}>
            {c.deleted ? "삭제된 댓글입니다." : c.content}
          </p>
        ) : (
          <div className={styles.editRow}>
            <input
              className={styles.input}
              value={editDraft}
              onChange={(e) => actions.onEditChange(c.id, e.target.value)}
            />
            <button className={styles.submit} onClick={() => actions.onSubmitEdit(c.id)}>
              <span className="material-symbols-outlined" style={{ fontSize: 16 }}>check</span>
            </button>
            <button className={styles.cancelBtn} onClick={() => actions.onCancelEdit(c.id)}>취소</button>
          </div>
        )}

        {!c.deleted && (
          <div className={styles.actions}>
            <button
              className={`${styles.likeBtn}${c.likedByMe ? ` ${styles.liked}` : ""}`}
              onClick={() => actions.onToggleLike(c)}
            >
              <span className={`material-symbols-outlined${c.likedByMe ? " filled" : ""}`} style={{ fontSize: 14 }}>
                favorite
              </span>
              {c.likeCount}
            </button>
            <button onClick={() => actions.onStartReply(c.id)}>답글</button>
            {isMine && editDraft === undefined && (
              <button onClick={() => actions.onStartEdit(c)}>수정</button>
            )}
            {isMine && <button onClick={() => actions.onDelete(c.id)}>삭제</button>}
          </div>
        )}

        {c.replies?.length > 0 && (
          <div className={styles.replies}>
            {c.replies.map((r) => (
              <CommentRow key={r.id} comment={r} depth={depth + 1} currentUserId={currentUserId} actions={actions} />
            ))}
          </div>
        )}

        {c.id in actions.replyDrafts && (
          <div className={styles.replyForm}>
            <input
              className={styles.input}
              placeholder="대댓글 입력"
              value={replyDraft}
              onChange={(e) => actions.onReplyChange(c.id, e.target.value)}
            />
            <button className={styles.submit} onClick={() => actions.onSubmitReply(c.id)}>
              <span className="material-symbols-outlined" style={{ fontSize: 16 }}>send</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
