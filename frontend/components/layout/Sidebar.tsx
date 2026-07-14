"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { isLoggedIn } from "@/lib/auth";
import { logout } from "@/lib/authActions";
import styles from "./Sidebar.module.css";

const NAV_ITEMS = [
  { href: "/", label: "피드", icon: "grid_view" },
  { href: "/mypage", label: "마이페이지", icon: "account_circle" },
  { href: "/mypage/follow", label: "친구", icon: "group" },
  { href: "/channels", label: "채널", icon: "hub" },
];

function isActive(pathname: string, href: string): boolean {
  if (href === "/") return pathname === "/";
  return pathname === href;
}

// 데스크톱 좌측 레일 네비게이션. 로고/스튜디오 정보 + 4개 메뉴 + 로그인/로그아웃(하단, 로그인 상태에 따라 통일 표시).
export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  // SSR에서는 항상 false로 시작하고, 마운트 후 실제 로그인 상태로 갱신한다 (localStorage는 클라이언트에만 존재).
  const [loggedIn, setLoggedIn] = useState(false);

  useEffect(() => {
    setLoggedIn(isLoggedIn());
  }, [pathname]);

  const handleLogout = async () => {
    await logout();
    router.push("/login");
  };

  return (
    <aside className={`${styles.sidebar} glass`}>
      <div className={styles.brand}>
        <div className={styles.brandIcon}>
          <span className="material-symbols-outlined">palette</span>
        </div>
        <div>
          <p className={styles.brandName}>크리에이티브 스튜디오</p>
          <p className={styles.brandSub}>Collaborative Space</p>
        </div>
      </div>

      <nav className={styles.nav}>
        {NAV_ITEMS.map((item) => {
          const active = isActive(pathname, item.href);
          return (
            <Link key={item.href} href={item.href} className={active ? styles.navItemActive : styles.navItem}>
              <span className="material-symbols-outlined">{item.icon}</span>
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>

      <div className={styles.footer}>
        {loggedIn ? (
          <button className={styles.logoutItem} type="button" onClick={handleLogout}>
            <span className="material-symbols-outlined">logout</span>
            <span>로그아웃</span>
          </button>
        ) : (
          <Link href="/login" className={styles.logoutItem}>
            <span className="material-symbols-outlined">login</span>
            <span>로그인</span>
          </Link>
        )}
      </div>
    </aside>
  );
}
