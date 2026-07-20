"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { isLoggedIn } from "@/lib/auth";
import styles from "./TopBar.module.css";

export default function TopBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("q") ?? "");

  useEffect(() => {
    setQuery(searchParams.get("q") ?? "");
  }, [searchParams]);

  const handleSearch = () => {
    const trimmed = query.trim();
    if (!trimmed) return;
    router.push(`/search?q=${encodeURIComponent(trimmed)}`);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleSearch();
  };

  return (
    <header className={styles.topBar}>
      <div />
      <div className={styles.searchWrap}>
        <span className={`material-symbols-outlined ${styles.searchIcon}`}>search</span>
        <input
          className={styles.input}
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="태그 또는 닉네임으로 검색"
          autoComplete="off"
        />
        {query && (
          <button
            className={styles.clearBtn}
            type="button"
            onClick={() => setQuery("")}
            aria-label="검색어 지우기"
          >
            <span className="material-symbols-outlined">close</span>
          </button>
        )}
      </div>
      <div>
        <Link
          href="/posts/new"
          className={styles.uploadBtn}
          onClick={(e) => {
            if (!isLoggedIn()) {
              e.preventDefault();
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
