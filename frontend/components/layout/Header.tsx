"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { isLoggedIn } from "@/lib/auth";
import styles from "./Header.module.css";

// 상단 GNB. 로고 + 팔로잉/채널 탭 + 검색 + 알림/업로드/프로필(로그인 시) 또는 로그인 링크(비로그인 시).
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
        <Link href="/" className={styles.logo}>
          <span className={styles.logoMark}>4NF</span>
          <span className={styles.logoType}>
            <span className={`${styles.logoTitle} brand-gradient-text`}>404</span>
            <span className={styles.logoSub}>Not Found</span>
          </span>
        </Link>
        <nav className={styles.tabs}>
          <Link href="/" className={pathname === "/" ? styles.tabActive : styles.tab}>
            팔로잉
          </Link>
          {/* TODO: 데모용 고정 id(1) -> 실제로는 팔로우 중인 채널 목록/추천에서 진입 */}
          <Link href="/channels/1" className={pathname.startsWith("/channels") ? styles.tabActive : styles.tab}>
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
        {loggedIn ? (
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
        ) : (
          <Link href="/login" className={styles.loginLink}>
            로그인
          </Link>
        )}
      </div>
    </header>
  );
}
