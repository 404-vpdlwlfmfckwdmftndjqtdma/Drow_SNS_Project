"use client";

import { ReactNode, useEffect, useState } from "react";
import api from "@/lib/api";
import { subscribeToPostFeed } from "@/lib/postFeedStream";
import CommentModal from "./CommentModal";
import styles from "./CommentButton.module.css";

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
}

interface CommentCountResult {
  count: number;
}

interface CommentButtonProps {
  postId: number;
  userId: number | null;
  initialCommentCount?: number;
  children?: ReactNode; // 버튼 라벨 (기본: 말풍선 아이콘 + "댓글")
}

/**
 * [comment 버튼 모듈] 태그로 꽂아 쓰는 재사용 댓글 버튼.
 * 클릭 → CommentModal 팝업으로 댓글 목록/작성/수정/삭제 UI를 띄운다.
 */
export default function CommentButton({ postId, userId, initialCommentCount = 0, children }: CommentButtonProps) {
  const [open, setOpen] = useState(false);
  const [commentCount, setCommentCount] = useState(initialCommentCount);

  useEffect(() => {
    setCommentCount(initialCommentCount);
  }, [initialCommentCount]);

  useEffect(() => {
    api
      .get<ApiEnvelope<CommentCountResult>>(`/api/v1/posts/${postId}/comments/count`)
      .then((res) => setCommentCount(res.data.data.count))
      .catch(() => {
        // 카운트 조회 실패는 조용히 무시하고 SSE 업데이트를 기다린다.
      });
  }, [postId]);

  useEffect(() => {
    return subscribeToPostFeed(postId, { onCommentCount: setCommentCount });
  }, [postId]);

  return (
    <>
      <button type="button" className={styles.trigger} onClick={() => setOpen(true)}>
        {children ?? (
          <>
            <span className="material-symbols-outlined">mode_comment</span>
            댓글 {commentCount}
          </>
        )}
      </button>
      {open && <CommentModal postId={postId} userId={userId} onClose={() => setOpen(false)} />}
    </>
  );
}
