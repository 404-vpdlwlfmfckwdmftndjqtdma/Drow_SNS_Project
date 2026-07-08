"use client";

import styles from "./page.module.css";

// TODO: 이메일/비밀번호 폼 -> POST /api/v1/auth/login -> setTokens() -> 홈으로 리다이렉트
export default function LoginPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>로그인</h1>
      <form className={styles.form}>
        <input className={styles.input} type="email" placeholder="이메일" />
        <input className={styles.input} type="password" placeholder="비밀번호" />
        <button className={styles.submit} type="submit">로그인</button>
      </form>
    </main>
  );
}
