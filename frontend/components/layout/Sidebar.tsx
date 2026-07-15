"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { isLoggedIn } from "@/lib/auth";
import { logout } from "@/lib/authActions";
import Logo from "@/components/common/Logo";
import styles from "./Sidebar.module.css";

const NAV_ITEMS = [
  { href: "/posts", label: "피드", icon: "grid_view", requiresAuth: false },
  { href: "/mypage", label: "마이페이지", icon: "account_circle", requiresAuth: true },
  { href: "/mypage/follow", label: "친구", icon: "group", requiresAuth: true },
  { href: "/channels", label: "채널", icon: "hub", requiresAuth: false },
];

function isActive(pathname: string, href: string): boolean {
  if (href === "/posts") return pathname === "/posts" || pathname.startsWith("/posts/");
  if (href.startsWith("/channels")) return pathname === "/channels" || pathname.startsWith("/channels/");
  return pathname === href;
}

// 데스크톱 좌측 레일 네비게이션. 로고 + 4개 메뉴 + 로그인/로그아웃 토글.
export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  // SSR에서는 항상 false로 시작하고, 마운트 후 실제 로그인 상태로 갱신한다 (localStorage는 클라이언트에만 존재).
  const [loggedIn, setLoggedIn] = useState(false);
  const [fontsReady, setFontsReady] = useState(false);

  useEffect(() => {
    setLoggedIn(isLoggedIn());
  }, [pathname]);

  useEffect(() => {
    let mounted = true;

    document.fonts.ready.then(() => {
      if (mounted) {
        setFontsReady(true);
      }
    });

    return () => {
      mounted = false;
    };
  }, []);

  const handleLogout = async () => {
    await logout();
    setLoggedIn(false);
    router.push("/posts");
  };

  return (
    <aside className={`${styles.sidebar} ${fontsReady ? styles.sidebarReady : ""} glass`}>
      <Link href="/posts" className={styles.logo}>
        <Logo />
      </Link>

      <nav className={styles.nav}>
        {NAV_ITEMS.map((item) => {
          const active = isActive(pathname, item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={active ? styles.navItemActive : styles.navItem}
              onClick={(e) => {
                // 로그인 안 된 상태로 마이페이지/친구처럼 로그인이 필요한 메뉴를 누르면
                // 해당 화면으로 갔다가 빈 화면을 보여주는 대신 바로 로그인 화면으로 보낸다.
                if (item.requiresAuth && !loggedIn) {
                  e.preventDefault();
                  router.push("/login");
                }
              }}
            >
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
          <Link href="/login" className={styles.loginItem}>
            <span className="material-symbols-outlined">login</span>
            <span>로그인</span>
          </Link>
        )}
      </div>
    </aside>
  );
}
