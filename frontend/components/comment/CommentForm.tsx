"use client";

import { useState } from "react";
import api from "@/lib/api";
import styles from "./CommentForm.module.css";

interface CommentFormProps {
  postId: number;
  onSubmitted?: () => void;
}

export default function CommentForm({ postId, onSubmitted }: CommentFormProps) {
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    const trimmed = content.trim();
    if (!trimmed) return;

    setSubmitting(true);
    try {
      await api.post(
        `/api/v1/posts/${postId}/comments`,
        { content: trimmed, parentId: null },
        { headers: { "X-User-Id": "1" } }
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
        placeholder="댓글을 입력하세요"
        value={content}
        onChange={(event) => setContent(event.target.value)}
        disabled={submitting}
      />
      <button className={styles.submit} type="submit" disabled={submitting}>
        {submitting ? "등록 중..." : "등록"}
      </button>
    </form>
  );
}
