"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { isLoggedIn } from "@/lib/auth";
import styles from "./Header.module.css";

// 상단 GNB. 로고 + 팔로잉/채널 탭 + 검색 + 알림/업로드/프로필(로그인 시). 로그인/로그아웃은 좌측 사이드바 하단으로 통일.
export default function Header() {
  const pathname = usePathname();
  // SSR에서는 항상 false로 시작하고, 마운트 후 실제 로그인 상태로 갱신한다 (localStorage는 클라이언트에만 존재).
  const [loggedIn, setLoggedIn] = useState(false);

  useEffect(() => {
    setLoggedIn(isLoggedIn());
  }, [pathname]);

  return (
    <header className={`${styles.header} glass`}>
      <div className={styles.left}>
        <Link href="/" className={`${styles.logo} brand-gradient-text`}>
          CanvasFlow
        </Link>
        <nav className={styles.tabs}>
          <Link href="/" className={pathname === "/" ? styles.tabActive : styles.tab}>
            팔로잉
          </Link>
          <Link href="/channels" className={pathname === "/channels" ? styles.tabActive : styles.tab}>
            채널
          </Link>
        </nav>
      </div>

      <div className={styles.search}>
        <span className={`material-symbols-outlined ${styles.searchIcon}`}>search</span>
        {/* TODO: 검색어 입력 -> /search?keyword= 로 이동 */}
        <input className={styles.searchInput} type="text" placeholder="영감을 주는 작품 검색..." />
      </div>

      <div className={styles.right}>
        {loggedIn && (
          <>
            <Link href="/notifications" className={styles.iconButton}>
              <span className="material-symbols-outlined">notifications</span>
            </Link>
            <Link href="/posts/new" className={styles.uploadButton}>
              업로드
            </Link>
            <Link href="/mypage" className={styles.avatar}>
              {/* TODO: 로그인한 사용자 프로필 이미지로 교체 */}
              <span className="material-symbols-outlined">account_circle</span>
            </Link>
          </>
        )}
      </div>
    </header>
  );
}
