"use client";

import { Suspense, useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "axios";
import api from "@/lib/api";
import styles from "./page.module.css";

// 비밀번호 찾기 메일의 링크(/reset-password?token=...)로 들어오는 화면.
// POST /api/v1/auth/password/reset 으로 토큰 + 새 비밀번호를 보낸다.
// 토큰이 없거나(직접 주소로 들어온 경우), 만료/이미 사용된 토큰이면 에러 메시지를 보여준다.
function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");

    if (password !== passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }
    if (password.length < 8) {
      setError("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    setSubmitting(true);
    try {
      await api.post("/api/v1/auth/password/reset", { token, newPassword: password });
      setDone(true);
    } catch (err) {
      const message = axios.isAxiosError(err) ? (err.response?.data as { message?: string } | undefined)?.message : undefined;
      setError(message ?? "비밀번호 재설정에 실패했습니다.");
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
              <h1 className={styles.title}>비밀번호 재설정</h1>
              <p className={styles.subtitle}>새로 사용할 비밀번호를 입력해주세요.</p>
            </div>

            {!token ? (
              <p className={styles.errorText}>
                유효하지 않은 접근입니다. 메일에 있는 링크를 다시 확인해주세요.
              </p>
            ) : done ? (
              <p className={styles.successText}>
                비밀번호가 변경되었습니다. 이제 새 비밀번호로 로그인해주세요.
              </p>
            ) : (
              <form className={styles.form} onSubmit={handleSubmit}>
                <div className={styles.field}>
                  <label className={styles.label} htmlFor="password">새 비밀번호</label>
                  <input
                    className={styles.input}
                    id="password"
                    type="password"
                    placeholder="8자 이상"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                <div className={styles.field}>
                  <label className={styles.label} htmlFor="passwordConfirm">새 비밀번호 확인</label>
                  <input
                    className={styles.input}
                    id="passwordConfirm"
                    type="password"
                    placeholder="8자 이상"
                    value={passwordConfirm}
                    onChange={(e) => setPasswordConfirm(e.target.value)}
                    required
                  />
                </div>

                {error && <p className={styles.errorText}>{error}</p>}

                <button className={styles.submit} type="submit" disabled={submitting}>
                  {submitting ? "변경 중..." : "비밀번호 변경"}
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

// useSearchParams는 Suspense 경계 안에서 써야 하는 Next.js 규칙 때문에 감싸준다.
export default function ResetPasswordPage() {
  return (
    <Suspense fallback={null}>
      <ResetPasswordForm />
    </Suspense>
  );
}
