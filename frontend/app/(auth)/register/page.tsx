"use client";

import styles from "./page.module.css";

// TODO: 이메일/비밀번호/닉네임 폼 -> POST /api/v1/auth/signup -> 로그인 페이지로 이동
export default function RegisterPage() {
  return (
    <main className={styles.container}>
      <h1 className={styles.title}>회원가입</h1>
      <form className={styles.form}>
        <input className={styles.input} type="email" placeholder="이메일" />
        <input className={styles.input} type="password" placeholder="비밀번호" />
        <input className={styles.input} type="text" placeholder="닉네임" />
        <button className={styles.submit} type="submit">가입하기</button>
      </form>
    </main>
  );
}
