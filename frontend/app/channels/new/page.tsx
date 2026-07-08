"use client";

import styles from "./page.module.css";

// TODO: 채널명/설명/기본 공개범위 입력 -> POST /api/v1/channels
export default function NewChannelPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>채널 개설</h1>
      <form className={styles.form}>
        <input className={styles.input} type="text" placeholder="채널명" />
        <textarea className={styles.textarea} placeholder="채널 소개" />
        <button className={styles.submit} type="submit">개설하기</button>
      </form>
    </main>
  );
}
