"use client";

import styles from "./Header.module.css";

// TODO: 로고, 검색창, 알림 아이콘, 프로필 메뉴
export default function Header() {
  return (
    <header className={styles.header}>
      <span className={styles.logo}>CanvasFlow</span>
      <div className={styles.actions}>{/* TODO */}</div>
    </header>
  );
}
