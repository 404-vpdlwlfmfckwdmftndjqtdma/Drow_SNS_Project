"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./BottomNav.module.css";

// 모바일 하단 탭바. 홈 / 탐색 / 작성(elevated) / 라이브 / 프로필
export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className={`${styles.nav} glass`}>
      <Link href="/" className={pathname === "/" ? styles.itemActive : styles.item}>
        <span className="material-symbols-outlined">home</span>
        <span>홈</span>
      </Link>
      <Link href="/search" className={pathname === "/search" ? styles.itemActive : styles.item}>
        <span className="material-symbols-outlined">explore</span>
        <span>탐색</span>
      </Link>
      <Link href="/posts/new" className={styles.addButton}>
        <span className="material-symbols-outlined">add</span>
      </Link>
      {/* TODO: 라이브(실시간 협업) 기능은 2차 확장 범위 */}
      <span className={styles.item}>
        <span className="material-symbols-outlined">sensors</span>
        <span>라이브</span>
      </span>
      <Link href="/mypage" className={pathname === "/mypage" ? styles.itemActive : styles.item}>
        <span className="material-symbols-outlined">account_circle</span>
        <span>프로필</span>
      </Link>
    </nav>
  );
}
