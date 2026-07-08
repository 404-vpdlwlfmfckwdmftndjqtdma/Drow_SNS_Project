"use client";

import Link from "next/link";
import styles from "./page.module.css";

// TODO: 이메일/비밀번호 폼 -> POST /api/v1/auth/login -> setTokens() -> 홈으로 리다이렉트
export default function LoginPage() {
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

            <form className={styles.form} onSubmit={(e) => e.preventDefault()}>
              <div className={styles.field}>
                <label className={styles.label} htmlFor="email">이메일 주소</label>
                <input className={styles.input} id="email" type="email" placeholder="name@example.com" />
              </div>

              <div className={styles.field}>
                <div className={styles.labelRow}>
                  <label className={styles.label} htmlFor="password">비밀번호</label>
                  <Link href="#" className={styles.forgotLink}>비밀번호를 잊으셨나요?</Link>
                </div>
                <input className={styles.input} id="password" type="password" placeholder="••••••••" />
              </div>

              <label className={styles.checkboxRow}>
                <input type="checkbox" />
                로그인 상태 유지
              </label>

              {/* TODO: 로그인 성공 시 홈으로 이동 */}
              <button className={styles.submit} type="submit">
                로그인
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
