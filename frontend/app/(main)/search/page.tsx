"use client";

import { useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
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
  const [mode, setMode] = useState<SearchMode>("tag");
  const [query, setQuery] = useState("");
  const [postResults, setPostResults] = useState<PostResult[]>([]);
  const [userResults, setUserResults] = useState<UserResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const handleSearch = async () => {
    const trimmed = query.trim();
    if (!trimmed) return;
    setLoading(true);
    setSearched(true);
    try {
      if (mode === "tag") {
        const res = await api.get<ApiResponse<PostResult[]>>("/api/v1/posts/search", {
          params: { tag: trimmed },
        });
        setPostResults(res.data.data);
        setUserResults([]);
      } else {
        const res = await api.get<ApiResponse<UserResult[]>>("/api/v1/users/search", {
          params: { nickname: trimmed },
        });
        setUserResults(res.data.data);
        setPostResults([]);
      }
    } catch {
      setPostResults([]);
      setUserResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleSearch();
  };

  const placeholder = mode === "tag" ? "태그 검색 (예: 포켓몬)" : "닉네임 검색";

  return (
    <main className={styles.container}>
      <h1 className={styles.title}>검색</h1>

      <div className={styles.tabs}>
        <button
          className={mode === "tag" ? styles.tabActive : styles.tab}
          onClick={() => { setMode("tag"); setSearched(false); setPostResults([]); setUserResults([]); }}
          type="button"
        >
          <span className="material-symbols-outlined">tag</span>
          태그
        </button>
        <button
          className={mode === "user" ? styles.tabActive : styles.tab}
          onClick={() => { setMode("user"); setSearched(false); setPostResults([]); setUserResults([]); }}
          type="button"
        >
          <span className="material-symbols-outlined">person_search</span>
          사용자
        </button>
      </div>

      <div className={styles.searchBar}>
        <input
          className={styles.input}
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
        />
        <button className={styles.searchBtn} onClick={handleSearch} type="button">
          <span className="material-symbols-outlined">search</span>
        </button>
      </div>

      {loading && <p className={styles.empty}>검색 중...</p>}

      {!loading && searched && mode === "tag" && (
        postResults.length === 0
          ? <p className={styles.empty}>검색 결과가 없습니다.</p>
          : <div className={styles.postList}>
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
                    <p className={styles.postContent}>{post.content.slice(0, 80)}{post.content.length > 80 ? "…" : ""}</p>
                    <div className={styles.postTags}>
                      {post.tags.map((tag) => (
                        <span key={tag} className={styles.tag}>#{tag}</span>
                      ))}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
      )}

      {!loading && searched && mode === "user" && (
        userResults.length === 0
          ? <p className={styles.empty}>검색 결과가 없습니다.</p>
          : <div className={styles.userList}>
              {userResults.map((user) => {
                const avatarStyle = getAvatarStyle(user.id);
                const initials = user.nickname.slice(0, 2);
                return (
                  <Link key={user.id} href={`/users/${user.id}`} className={styles.userCard}>
                    {user.profileImageUrl
                      ? <img src={user.profileImageUrl} alt={user.nickname} className={styles.userAvatar} />
                      : <div className={styles.userAvatar} style={{ background: avatarStyle.bg, color: avatarStyle.color }}>{initials}</div>
                    }
                    <div>
                      <p className={styles.userName}>{user.nickname}</p>
                      {user.bio && <p className={styles.userBio}>{user.bio}</p>}
                    </div>
                  </Link>
                );
              })}
            </div>
      )}
    </main>
  );
}
