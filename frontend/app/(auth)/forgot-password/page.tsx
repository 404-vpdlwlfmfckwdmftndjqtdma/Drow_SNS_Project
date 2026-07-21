"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import axios from "axios";
import api from "@/lib/api";
import styles from "./page.module.css";

// POST /api/v1/auth/password/reset-request 로 연동.
// 가입 여부와 무관하게 백엔드가 항상 같은 성공 메시지를 주기 때문에(이메일 존재 여부 비노출),
// 실패는 네트워크 오류 등 진짜 예외 상황에서만 표시한다.
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await api.post("/api/v1/auth/password/reset-request", { email });
      setSubmitted(true);
    } catch (err) {
      const message = axios.isAxiosError(err) ? (err.response?.data as { message?: string } | undefined)?.message : undefined;
      setError(message ?? "요청 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setSubmitting(false);
    }
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

                {error && <p className={styles.errorText}>{error}</p>}

                <button className={styles.submit} type="submit" disabled={submitting}>
                  {submitting ? "전송 중..." : "재설정 링크 보내기"}
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
