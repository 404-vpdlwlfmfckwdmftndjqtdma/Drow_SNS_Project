"use client";

import { useState } from "react";
import api from "@/lib/api";
import styles from "./CommentForm.module.css";

interface CommentFormProps {
  postId: number;
  userId: number | null;
  onSubmitted?: () => void;
}

export default function CommentForm({ postId, userId, onSubmitted }: CommentFormProps) {
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (userId == null) {
      alert("댓글 작성은 로그인 후 사용할 수 있습니다.");
      return;
    }

    const trimmed = content.trim();
    if (!trimmed) return;

    setSubmitting(true);
    try {
      await api.post(
        `/api/v1/posts/${postId}/comments`,
        { content: trimmed, parentId: null },
        { headers: { "X-User-Id": String(userId) } }
      );
      setContent("");
      onSubmitted?.();
    } catch {
      alert("댓글 등록에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <input
        className={styles.input}
        type="text"
        placeholder={userId == null ? "로그인 후 댓글을 작성할 수 있습니다" : "댓글을 입력하세요"}
        value={content}
        onChange={(event) => setContent(event.target.value)}
        disabled={submitting || userId == null}
      />
      <button className={styles.submit} type="submit" disabled={submitting || userId == null}>
        {submitting ? "등록 중..." : "등록"}
      </button>
    </form>
  );
}
