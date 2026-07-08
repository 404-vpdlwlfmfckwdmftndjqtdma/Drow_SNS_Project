"use client";

import styles from "./page.module.css";

// TODO: 제목/본문/태그/채널 선택 + components/post/MediaUploader 로 이미지·영상 첨부 후
//       POST /api/v1/posts 호출
export default function NewPostPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>게시글 작성</h1>
      <form className={styles.form}>
        <input className={styles.input} type="text" placeholder="제목" />
        <textarea className={styles.textarea} placeholder="내용" />
        {/* TODO: MediaUploader, 공개범위/채널/태그 선택 */}
        <button className={styles.submit} type="submit">게시하기</button>
      </form>
    </main>
  );
}
