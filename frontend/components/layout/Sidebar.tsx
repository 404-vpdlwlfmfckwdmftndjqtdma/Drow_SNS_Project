"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import api from "@/lib/api";
import { getCurrentUserId, isLoggedIn } from "@/lib/auth";
import { logout } from "@/lib/authActions";
import Logo from "@/components/common/Logo";
import type { ApiResponse } from "@/types";
import styles from "./Sidebar.module.css";

const ACCOUNT_NAV_ITEMS = [
  { href: "/mypage/following", label: "팔로우", icon: "group", requiresAuth: true },
  { href: "/mypage/likes", label: "좋아요", icon: "favorite", requiresAuth: true },
  { href: "/mypage/comments", label: "댓글", icon: "chat_bubble", requiresAuth: true },
  { href: "/payment", label: "결제", icon: "credit_card", requiresAuth: true },
];

function isActive(pathname: string, href: string): boolean {
  if (href === "/payment") return pathname === "/payment" || pathname.startsWith("/payment/");
  return pathname === href;
}

// 데스크톱 좌측 레일 네비게이션. 기본 화면에서는 메뉴를 비우고,
// 내 프로필/결제/채널/유저 프로필(팔로워 목록 포함) 영역에서만 계정 관련 메뉴를 보여준다.
export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  // SSR에서는 항상 false로 시작하고, 마운트 후 실제 로그인 상태로 갱신한다 (localStorage는 클라이언트에만 존재).
  const [loggedIn, setLoggedIn] = useState(false);
  const [fontsReady, setFontsReady] = useState(false);
  // 프로필 풀다운 메뉴 열림 상태
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);
  const profileRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setLoggedIn(isLoggedIn());
    setMenuOpen(false); // 화면 이동 시 풀다운 닫기
  }, [pathname]);

  // 프로필 칩 아바타용 이미지 조회. 실패해도 기본 person 아이콘으로 대체되므로 조용히 무시한다.
  useEffect(() => {
    if (!loggedIn) {
      setProfileImageUrl(null);
      return;
    }
    const userId = getCurrentUserId();
    if (userId == null) return;

    api
      .get<ApiResponse<{ profileImageUrl: string | null }>>(`/api/v1/users/${userId}`)
      .then((res) => setProfileImageUrl(res.data.data.profileImageUrl))
      .catch(() => {});
  }, [loggedIn]);

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

  const isAccountSection =
    pathname.startsWith("/mypage") ||
    pathname.startsWith("/payment") ||
    pathname.startsWith("/channels") ||
    pathname.startsWith("/users");
  const navItems = isAccountSection ? ACCOUNT_NAV_ITEMS : [];

  return (
    <aside className={`${styles.sidebar} ${fontsReady ? styles.sidebarReady : ""} glass`}>
      <Link href="/posts" className={styles.logo}>
        <Logo />
      </Link>

      <div className={styles.profileArea} ref={profileRef}>
        {loggedIn ? (
          <>
            <button className={styles.profileChip} type="button" onClick={() => setMenuOpen((v) => !v)}>
              {/* 그라데이션 스토리 링 + 원형 아바타. 프로필 이미지가 있으면 그걸, 없으면 기본 person 아이콘 */}
              <span className={styles.avatarRing}>
                <span
                  className={styles.profileAvatar}
                  style={
                    profileImageUrl
                      ? { backgroundImage: `url(${profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
                      : undefined
                  }
                >
                  {!profileImageUrl && <span className="material-symbols-outlined">person</span>}
                </span>
              </span>
              <span className={styles.profileCopy}>
                <span className={styles.profileName}>내 프로필</span>
                <span className={styles.profileMeta}>계정 메뉴</span>
              </span>
              <span
                className={`material-symbols-outlined ${styles.profileChevron} ${menuOpen ? styles.profileChevronOpen : ""}`}
              >
                expand_more
              </span>
            </button>
            {menuOpen && (
              <div className={styles.profileMenu}>
                <Link href="/mypage" className={styles.profileMenuItem} onClick={() => setMenuOpen(false)}>
                  <span className="material-symbols-outlined">account_circle</span>
                  <span>내 프로필</span>
                </Link>
                <button className={styles.profileMenuDanger} type="button" onClick={handleLogout}>
                  <span className="material-symbols-outlined">logout</span>
                  <span>로그아웃</span>
                </button>
              </div>
            )}
          </>
        ) : (
          <Link href="/login" className={`${styles.profileChip} ${styles.loginChip}`}>
            <span className={styles.avatarRing}>
              <span className={styles.profileAvatar}>
                <span className="material-symbols-outlined">person</span>
              </span>
            </span>
            <span className={styles.profileCopy}>
              <span className={styles.profileName}>로그인</span>
              <span className={styles.profileMeta}>계정을 연결하세요</span>
            </span>
            <span className={`material-symbols-outlined ${styles.profileChevron}`}>arrow_forward</span>
          </Link>
        )}
      </div>

      <nav className={styles.nav}>
        {navItems.map((item) => {
          const active = isActive(pathname, item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={active ? styles.navItemActive : styles.navItem}
              onClick={(e) => {
                // 로그인 안 된 상태로 계정 메뉴를 누르면
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
