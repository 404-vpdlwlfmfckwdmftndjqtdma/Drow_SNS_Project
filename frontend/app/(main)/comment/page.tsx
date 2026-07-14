"use client";

import { useState } from "react";
import CommentButton from "@/components/comment/CommentButton";
import PostLikeButton from "@/components/post/PostLikeButton";
import NotificationButton from "@/components/notification/NotificationButton";
import styles from "./page.module.css";

// 댓글 모듈 데모 페이지.
// auth 도메인 JWT 로그인이 아직 안 되는 상태라(refresh_tokens 테이블 미비), 다른 도메인들과 동일하게
// X-User-Id 헤더로 사용자를 직접 지정한다 (userId 입력창에 DB에 실제 존재하는 유저 id를 넣으면 됨).
// 댓글 CRUD 로직은 components/comment/CommentButton, CommentModal 로 모듈화되어 있다.

export default function CommentPage() {
  const [postId, setPostId] = useState(1);
  const [userId, setUserId] = useState(1);

  return (
    <main className={styles.container}>
      <h1>댓글 모듈</h1>
      <p className={styles.warning}>
        JWT 로그인 대신 X-User-Id 헤더로 사용자를 지정합니다 (DB에 실제 존재하는 user id 입력).
      </p>

      <div className={styles.row}>
        <label>postId</label>
        <input
          className={styles.input}
          type="number"
          value={postId}
          onChange={(e) => setPostId(Number(e.target.value))}
        />
        <label>userId (X-User-Id)</label>
        <input
          className={styles.input}
          type="number"
          value={userId}
          onChange={(e) => setUserId(Number(e.target.value))}
        />
      </div>

      <div className={styles.row}>
        <PostLikeButton key={`${postId}-${userId}`} postId={postId} userId={userId} />
        <CommentButton postId={postId} userId={userId} />
        <NotificationButton key={`bell-${userId}`} userId={userId} />
      </div>
    </main>
  );
}
