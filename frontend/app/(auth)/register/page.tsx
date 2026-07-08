"use client";

import Link from "next/link";
import styles from "./page.module.css";

// TODO: 이름/이메일/비밀번호 폼 -> POST /api/v1/auth/signup -> 로그인 페이지로 이동
export default function RegisterPage() {
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
              <h1 className={styles.title}>계정 만들기</h1>
              <p className={styles.subtitle}>창작의 즐거움을 함께 나누는 커뮤니티에 합류하세요.</p>
            </div>

            <form className={styles.form} onSubmit={(e) => e.preventDefault()}>
              <div className={styles.field}>
                <label className={styles.label} htmlFor="name">이름</label>
                <input className={styles.input} id="name" type="text" placeholder="홍길동" />
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="email">이메일 주소</label>
                <input className={styles.input} id="email" type="email" placeholder="name@example.com" />
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="password">비밀번호</label>
                <input className={styles.input} id="password" type="password" placeholder="••••••••" />
              </div>

              <label className={styles.checkboxRow}>
                <input type="checkbox" />
                <span>
                  <Link href="#" className={styles.forgotLink}>이용 약관</Link> 및{" "}
                  <Link href="#" className={styles.forgotLink}>개인정보 처리방침</Link>에 동의하며, CanvasFlow의
                  커뮤니티 가이드라인을 준수합니다.
                </span>
              </label>

              {/* TODO: 회원가입 성공 시 로그인 페이지로 이동 */}
              <button className={styles.submit} type="submit">
                시작하기
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
