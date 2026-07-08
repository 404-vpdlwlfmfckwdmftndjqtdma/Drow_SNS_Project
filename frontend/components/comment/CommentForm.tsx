"use client";

import styles from "./CommentForm.module.css";

// TODO: POST /api/v1/posts/{postId}/comments
export default function CommentForm({ postId }: { postId: number }) {
  return (
    <form className={styles.form}>
      <input className={styles.input} type="text" placeholder="댓글을 입력하세요" />
      <button className={styles.submit} type="submit">등록</button>
    </form>
  );
}
