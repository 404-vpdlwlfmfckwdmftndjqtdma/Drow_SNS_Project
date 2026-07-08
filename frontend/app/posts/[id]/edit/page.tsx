"use client";

import styles from "./page.module.css";

// TODO: 기존 게시글 값 불러와 폼 채우기 -> PUT /api/v1/posts/{id}
export default async function EditPostPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>게시글 수정 #{id}</h1>
      <form className={styles.form}>
        <input className={styles.input} type="text" placeholder="제목" />
        <textarea className={styles.textarea} placeholder="내용" />
        <button className={styles.submit} type="submit">수정 완료</button>
      </form>
    </main>
  );
}
