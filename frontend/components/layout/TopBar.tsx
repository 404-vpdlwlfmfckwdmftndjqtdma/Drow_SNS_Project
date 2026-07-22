"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { AUTH_CHANGE_EVENT, getCurrentUserId, isLoggedIn } from "@/lib/auth";
import NotificationButton from "@/components/notification/NotificationButton";
import WalletChip from "@/components/payment/WalletChip";
import styles from "./TopBar.module.css";

// 검색창(중앙) + 업로드/알림(우측)을 담는 상단 고정 바. 어느 화면에서든 항상 떠 있는 전역 액션이라
// (계정 메뉴에서만 보이는 사이드바 내비게이션과 달리) 사이드바가 아니라 여기에 모아둔다.
export default function TopBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("q") ?? "");
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  useEffect(() => {
    setQuery(searchParams.get("q") ?? "");
  }, [searchParams]);

  useEffect(() => {
    const syncCurrentUser = () => setCurrentUserId(getCurrentUserId());
    syncCurrentUser();

    window.addEventListener(AUTH_CHANGE_EVENT, syncCurrentUser);
    window.addEventListener("storage", syncCurrentUser);
    return () => {
      window.removeEventListener(AUTH_CHANGE_EVENT, syncCurrentUser);
      window.removeEventListener("storage", syncCurrentUser);
    };
  }, []);

  function handleSearchKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key !== "Enter") return;
    const trimmed = query.trim();
    if (!trimmed) return;
    router.push(`/search?q=${encodeURIComponent(trimmed)}`);
  }

  return (
    <header className={`${styles.topBar} glass`}>
      <div />

      <div className={styles.searchWrap}>
        <span className="material-symbols-outlined">search</span>
        <input
          className={styles.searchInput}
          placeholder="태그, 유저 이름을 입력하세요"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={handleSearchKeyDown}
        />
      </div>

      <div className={styles.actions}>
        {currentUserId != null && <NotificationButton userId={currentUserId} />}
        {/* 업로드 버튼 왼쪽에 보유 토큰 표시 (누르면 충전으로 이동) */}
        <WalletChip />
        <Link
          href="/posts/new"
          className={styles.uploadBtn}
          onClick={(event) => {
            if (!isLoggedIn()) {
              event.preventDefault();
              router.push("/login");
            }
          }}
        >
          <span className="material-symbols-outlined">upload</span>
          업로드
        </Link>
      </div>
    </header>
  );
}
