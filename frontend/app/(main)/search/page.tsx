"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import api from "@/lib/api";
import { getCurrentUserId, AUTH_CHANGE_EVENT } from "@/lib/auth";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

type SearchMode = "tag" | "user";

interface PostResult {
  postId: number;
  userId: number;
  content: string;
  tags: string[];
  media: { url: string; mediaType: "IMAGE" | "VIDEO" }[];
  viewCount: number;
  createdAt: string;
  nickname?: string;
}

interface UserResult {
  id: number;
  nickname: string;
  profileImageUrl?: string;
  bio?: string;
}

const AVATAR_PALETTES = [
  { bg: "#ede8fb", color: "#6b38d4" },
  { bg: "#e6edf9", color: "#0058be" },
  { bg: "#fff3e0", color: "#994100" },
  { bg: "#e1f5ee", color: "#0f6e56" },
  { bg: "#fbeaf0", color: "#993556" },
];

function getAvatarStyle(id: number) {
  return AVATAR_PALETTES[id % AVATAR_PALETTES.length];
}

export default function SearchPage() {
  const searchParams = useSearchParams();
  const q = searchParams.get("q") ?? "";

  const [mode, setMode] = useState<SearchMode>("tag");
  const [postResults, setPostResults] = useState<PostResult[]>([]);
  const [userResults, setUserResults] = useState<UserResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  useEffect(() => {
    const sync = () => setCurrentUserId(getCurrentUserId());
    sync();
    window.addEventListener(AUTH_CHANGE_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_CHANGE_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  const runSearch = useCallback(async (keyword: string) => {
    setLoading(true);
    setSearched(true);
    try {
      const [postsRes, usersRes] = await Promise.allSettled([
        api.get<ApiResponse<PostResult[]>>("/api/v1/search/posts", { params: { tag: keyword } }),
        api.get<ApiResponse<UserResult[]>>("/api/v1/search/users", { params: { nickname: keyword } }),
      ]);
      setPostResults(postsRes.status === "fulfilled" ? postsRes.value.data.data : []);
      setUserResults(usersRes.status === "fulfilled" ? usersRes.value.data.data : []);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (q) {
      runSearch(q);
    } else {
      setSearched(false);
      setPostResults([]);
      setUserResults([]);
    }
  }, [q, runSearch]);

  const resultCount = mode === "tag" ? postResults.length : userResults.length;

  return (
    <main className={styles.container}>
      <div className={styles.hero}>
        <h1 className={styles.title}>검색</h1>
        {q && <p className={styles.subtitle}>"{q}" 검색 결과</p>}
      </div>

      <div className={styles.tabs}>
        <button
          className={mode === "tag" ? styles.tabActive : styles.tab}
          onClick={() => setMode("tag")}
          type="button"
        >
          <span className="material-symbols-outlined">tag</span>
          태그
          {searched && <span className={styles.tabCount}>{postResults.length}</span>}
        </button>
        <button
          className={mode === "user" ? styles.tabActive : styles.tab}
          onClick={() => setMode("user")}
          type="button"
        >
          <span className="material-symbols-outlined">person_search</span>
          사용자
          {searched && <span className={styles.tabCount}>{userResults.length}</span>}
        </button>
      </div>

      {loading && (
        <div className={styles.emptyState}>
          <span className={`material-symbols-outlined ${styles.emptyIcon}`}>search</span>
          <p className={styles.emptyText}>검색 중...</p>
        </div>
      )}

      {!loading && !searched && (
        <div className={styles.emptyState}>
          <span className={`material-symbols-outlined ${styles.emptyIcon}`}>travel_explore</span>
          <p className={styles.emptyText}>상단 검색창에 키워드를 입력해보세요</p>
          <p className={styles.emptyHint}>태그와 사용자를 한 번에 검색합니다</p>
        </div>
      )}

      {!loading && searched && resultCount === 0 && (
        <div className={styles.emptyState}>
          <span className={`material-symbols-outlined ${styles.emptyIcon}`}>search_off</span>
          <p className={styles.emptyText}>
            {mode === "tag" ? `"${q}" 태그의 게시글이 없습니다` : `"${q}" 닉네임의 사용자가 없습니다`}
          </p>
          <p className={styles.emptyHint}>다른 키워드로 다시 시도해보세요</p>
        </div>
      )}

      {!loading && searched && mode === "tag" && postResults.length > 0 && (
        <>
          <p className={styles.resultCount}>게시글 {postResults.length}개</p>
          <div className={styles.postList}>
            {postResults.map((post) => (
              <Link key={post.postId} href={`/posts/${post.postId}`} className={styles.postCard}>
                {post.media.length > 0 && (
                  <div className={styles.postThumbnail}>
                    {post.media[0].mediaType === "VIDEO"
                      ? <video src={post.media[0].url} muted />
                      : <img src={post.media[0].url} alt="" />}
                  </div>
                )}
                <div className={styles.postBody}>
                  <p className={styles.postNickname}>{post.nickname ?? `유저 #${post.userId}`}</p>
                  <p className={styles.postContent}>{post.content}</p>
                  <div className={styles.postTags}>
                    {post.tags.map((tag) => (
                      <span key={tag} className={styles.tag}>#{tag}</span>
                    ))}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </>
      )}

      {!loading && searched && mode === "user" && userResults.length > 0 && (
        <>
          <p className={styles.resultCount}>사용자 {userResults.length}명</p>
          <div className={styles.userList}>
            {userResults.map((user) => {
              const avatarStyle = getAvatarStyle(user.id);
              const initials = user.nickname.slice(0, 2);
              return (
                <Link key={user.id} href={currentUserId === user.id ? "/mypage" : `/users/${user.id}`} className={styles.userCard}>
                  {user.profileImageUrl
                    ? <img src={user.profileImageUrl} alt={user.nickname} className={styles.userAvatar} />
                    : <div className={styles.userAvatar} style={{ background: avatarStyle.bg, color: avatarStyle.color }}>{initials}</div>
                  }
                  <div className={styles.userInfo}>
                    <p className={styles.userName}>{user.nickname}</p>
                    {user.bio && <p className={styles.userBio}>{user.bio}</p>}
                  </div>
                  <span className={`material-symbols-outlined ${styles.userArrow}`}>chevron_right</span>
                </Link>
              );
            })}
          </div>
        </>
      )}
    </main>
  );
}
