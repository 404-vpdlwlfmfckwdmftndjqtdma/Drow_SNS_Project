"use client";

import { ReactNode, useState } from "react";
import CommentModal from "./CommentModal";
import styles from "./CommentButton.module.css";

interface CommentButtonProps {
  postId: number;
  userId: number; // TODO: JWT 로그인 붙으면 X-User-Id 대신 Authorization 토큰 기반으로 전환
  children?: ReactNode; // 버튼 라벨 (기본: 말풍선 아이콘 + "댓글")
}

/**
 * [comment 버튼 모듈] 태그로 꽂아 쓰는 재사용 댓글 버튼.
 * 클릭 → CommentModal 팝업으로 댓글 목록/작성/수정/삭제 UI를 띄운다.
 */
export default function CommentButton({ postId, userId, children }: CommentButtonProps) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <button type="button" className={styles.trigger} onClick={() => setOpen(true)}>
        {children ?? (
          <>
            <span className="material-symbols-outlined">mode_comment</span>
            댓글
          </>
        )}
      </button>
      {open && <CommentModal postId={postId} userId={userId} onClose={() => setOpen(false)} />}
    </>
  );
}
