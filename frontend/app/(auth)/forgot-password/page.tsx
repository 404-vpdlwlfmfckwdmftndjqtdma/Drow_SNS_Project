"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import styles from "./page.module.css";

// TODO: 백엔드에 비밀번호 재설정 API가 아직 없음 (예: POST /api/v1/auth/password/reset-request).
// 백엔드 준비되면 이 폼의 onSubmit에서 실제 API 호출로 교체하고, 실패 시 에러 메시지도 표시할 것.
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
  };

  return (
    <div className={styles.page}>
      <section className={styles.formSide}>
        <div className={styles.formCenter}>
          <div className={styles.formInner}>
            <div>
              <h1 className={styles.title}>비밀번호 찾기</h1>
              <p className={styles.subtitle}>
                가입하신 이메일 주소를 입력하시면 비밀번호 재설정 링크를 보내드립니다.
              </p>
            </div>

            {submitted ? (
              <p className={styles.successText}>
                <strong>{email}</strong> 주소로 비밀번호 재설정 링크를 보내드렸습니다. 메일함을 확인해주세요.
              </p>
            ) : (
              <form className={styles.form} onSubmit={handleSubmit}>
                <div className={styles.field}>
                  <label className={styles.label} htmlFor="email">이메일 주소</label>
                  <input
                    className={styles.input}
                    id="email"
                    type="email"
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>

                <button className={styles.submit} type="submit">
                  재설정 링크 보내기
                  <span className="material-symbols-outlined">arrow_forward</span>
                </button>
              </form>
            )}

            <p className={styles.footerText}>
              <Link href="/login" className={styles.footerLink}>로그인으로 돌아가기</Link>
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}
