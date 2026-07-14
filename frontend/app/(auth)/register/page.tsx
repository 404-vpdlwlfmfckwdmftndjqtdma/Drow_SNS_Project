"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import Link from "next/link";
import api from "@/lib/api";
import styles from "./page.module.css";

// 이름 입력값은 백엔드 User 엔티티의 nickname(2~20자)에 대응된다.
export default function RegisterPage() {
  const router = useRouter();
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    if (password !== passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }
    setSubmitting(true);
    try {
      await api.post("/api/v1/auth/signup", { email, password, nickname });
      router.push("/login");
    } catch (err) {
      const message = axios.isAxiosError(err) ? (err.response?.data as { message?: string } | undefined)?.message : undefined;
      setError(message ?? "회원가입에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.visual}>
        <div className={styles.logoBadge}>
          <span className={styles.logoText}>CanvasFlow</span>
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
              <h1 className={styles.title}>계정 만들기</h1>
              <p className={styles.subtitle}>창작의 즐거움을 함께 나누는 커뮤니티에 합류하세요.</p>
            </div>

            <form className={styles.form} onSubmit={handleSubmit}>
              <div className={styles.field}>
                <label className={styles.label} htmlFor="name">이름</label>
                <input
                  className={styles.input}
                  id="name"
                  type="text"
                  placeholder="홍길동"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  minLength={2}
                  maxLength={20}
                  required
                />
              </div>

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
                <label className={styles.label} htmlFor="password">비밀번호</label>
                <input
                  className={styles.input}
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  minLength={8}
                  maxLength={64}
                  required
                />
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="passwordConfirm">비밀번호 확인</label>
                <input
                  className={styles.input}
                  id="passwordConfirm"
                  type="password"
                  placeholder="••••••••"
                  value={passwordConfirm}
                  onChange={(e) => setPasswordConfirm(e.target.value)}
                  minLength={8}
                  maxLength={64}
                  required
                />
              </div>

              {error && <p className={styles.errorText}>{error}</p>}

              <button className={styles.submit} type="submit" disabled={submitting}>
                {submitting ? "가입 중..." : "시작하기"}
                <span className="material-symbols-outlined">arrow_forward</span>
              </button>
            </form>

            <p className={styles.footerText}>
              이미 계정이 있으신가요?
              <Link href="/login" className={styles.footerLink}>로그인</Link>
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
