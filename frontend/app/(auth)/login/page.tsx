"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import Link from "next/link";
import api from "@/lib/api";
import { setTokens } from "@/lib/auth";
import styles from "./page.module.css";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const { data } = await api.post("/api/v1/auth/login", { email, password });
      const { accessToken, refreshToken } = data.data;
      setTokens(accessToken, refreshToken);
      router.push("/");
    } catch (err) {
      const message = axios.isAxiosError(err) ? (err.response?.data as { message?: string } | undefined)?.message : undefined;
      setError(message ?? "로그인에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.visual}>
        <div className={styles.collabBadge}>
          <div className={styles.avatarStack}>
            <span />
            <span />
            <span />
          </div>
          <span className={styles.collabLabel}>3명이 작업 중</span>
        </div>
        <div className={styles.brandPanel}>
          <h2 className={styles.brandTitle}>
            함께 그리는 상상, <span>창의력의 흐름.</span>
          </h2>
          <p className={styles.brandDesc}>전 세계 창작자들과 실시간으로 협업하고 당신의 영감을 구체화하세요.</p>
        </div>
      </section>

      <section className={styles.formSide}>
        <div className={styles.formCenter}>
          <div className={styles.formInner}>
            <div>
              <h1 className={styles.title}>로그인</h1>
              <p className={styles.subtitle}>창작의 즐거움을 함께 나누는 커뮤니티에 다시 오신 것을 환영합니다.</p>
            </div>

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

              <div className={styles.field}>
                <div className={styles.labelRow}>
                  <label className={styles.label} htmlFor="password">비밀번호</label>
                  <Link href="#" className={styles.forgotLink}>비밀번호를 잊으셨나요?</Link>
                </div>
                <input
                  className={styles.input}
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <label className={styles.checkboxRow}>
                <input type="checkbox" />
                로그인 상태 유지
              </label>

              {error && <p className={styles.errorText}>{error}</p>}

              <button className={styles.submit} type="submit" disabled={submitting}>
                {submitting ? "로그인 중..." : "로그인"}
                <span className="material-symbols-outlined">arrow_forward</span>
              </button>
            </form>

            <p className={styles.footerText}>
              계정이 없으신가요?
              <Link href="/register" className={styles.footerLink}>회원가입</Link>
            </p>
          </div>
        </div>

        <div className={styles.legalLinks}>
          <Link href="#">이용약관</Link>
          <Link href="#">개인정보처리방침</Link>
          <Link href="#">고객지원</Link>
        </div>
      </section>
    </div>
  );
}
