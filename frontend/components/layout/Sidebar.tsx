"use client";

import { useEffect, useRef, useState } from "react";
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

// 데스크톱 좌측 레일 네비게이션. 로고 + 프로필(로그인 토글) + 4개 메뉴.
export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  // SSR에서는 항상 false로 시작하고, 마운트 후 실제 로그인 상태로 갱신한다 (localStorage는 클라이언트에만 존재).
  const [loggedIn, setLoggedIn] = useState(false);
  const [fontsReady, setFontsReady] = useState(false);
  // 프로필 풀다운 메뉴 열림 상태
  const [menuOpen, setMenuOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setLoggedIn(isLoggedIn());
    setMenuOpen(false); // 화면 이동 시 풀다운 닫기
  }, [pathname]);

  // 풀다운 바깥 클릭 시 닫기
  useEffect(() => {
    if (!menuOpen) return;
    const close = (e: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [menuOpen]);

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
    setMenuOpen(false);
    router.push("/posts");
  };

  return (
    <aside className={`${styles.sidebar} ${fontsReady ? styles.sidebarReady : ""} glass`}>
      <Link href="/posts" className={styles.logo}>
        <Logo />
      </Link>

      <div className={styles.profileArea} ref={profileRef}>
        {loggedIn ? (
          <>
            <button className={styles.profileChip} type="button" onClick={() => setMenuOpen((v) => !v)}>
              {/* 그라데이션 스토리 링 + 원형 아바타. TODO: 로그인한 사용자 프로필 이미지로 교체 */}
              <span className={styles.avatarRing}>
                <span className={styles.profileAvatar}>
                  <span className="material-symbols-outlined">person</span>
                </span>
              </span>
              <span className={styles.profileName}>내 프로필</span>
              <span
                className={`material-symbols-outlined ${styles.profileChevron} ${menuOpen ? styles.profileChevronOpen : ""}`}
              >
                expand_more
              </span>
            </button>
            {menuOpen && (
              <div className={styles.profileMenu}>
                {/* TODO: 실제 결제/구독 페이지가 생기면 교체 (지금은 결제 테스트 페이지) */}
                <Link href="/test" className={styles.profileMenuItem} onClick={() => setMenuOpen(false)}>
                  <span className="material-symbols-outlined">credit_card</span>
                  <span>결제</span>
                </Link>
                <button className={styles.profileMenuDanger} type="button" onClick={handleLogout}>
                  <span className="material-symbols-outlined">logout</span>
                  <span>로그아웃</span>
                </button>
              </div>
            )}
          </>
        ) : (
          <Link href="/login" className={styles.loginItem}>
            <span className="material-symbols-outlined">login</span>
            <span>로그인</span>
          </Link>
        )}
      </div>

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
    </aside>
  );
}
