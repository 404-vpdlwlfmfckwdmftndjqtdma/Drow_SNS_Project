"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import api from "@/lib/api";
import { getCurrentUserId } from "@/lib/auth";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostViewDto와 1:1로 맞춘 응답 타입.
interface PostResult {
  postId: number;
  userId: number;
  nickname: string;
  profileImageUrl?: string | null;
  content: string;
  tags: string[];
  media: { url: string; mediaType: "IMAGE" | "VIDEO" }[];
  viewCount: number;
  createdAt: string;
}

// 백엔드 UserProfileView와 1:1로 맞춘 응답 타입.
interface UserResult {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
  bio: string | null;
}

type Tab = "post" | "user";

// 검색은 TopBar의 검색창이 담당한다 (여기서는 결과만 보여줌).
// 태그 검색(GET /api/v1/posts/search)과 닉네임 검색(GET /api/v1/users/search)을 동시에 호출하고,
// 탭으로 결과만 나눠서 보여준다 - 탭을 눌러도 재검색하지 않는다.
function SearchResults() {
  const searchParams = useSearchParams();
  const query = searchParams.get("q") ?? "";

  const [tab, setTab] = useState<Tab>("post");
  const [posts, setPosts] = useState<PostResult[]>([]);
  const [users, setUsers] = useState<UserResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  useEffect(() => {
    setCurrentUserId(getCurrentUserId());
  }, []);

  useEffect(() => {
    const trimmed = query.trim();
    if (!trimmed) {
      setPosts([]);
      setUsers([]);
      setSearched(false);
      return;
    }

    setLoading(true);
    setSearched(true);

    Promise.allSettled([
      api.get<ApiResponse<PostResult[]>>("/api/v1/posts/search", { params: { tag: trimmed } }),
      api.get<ApiResponse<UserResult[]>>("/api/v1/users/search", { params: { nickname: trimmed } }),
    ]).then(([postRes, userRes]) => {
      setPosts(postRes.status === "fulfilled" ? postRes.value.data.data : []);
      setUsers(userRes.status === "fulfilled" ? userRes.value.data.data : []);
      setLoading(false);
    });
  }, [query]);

  return (
    <main className={styles.container}>
      <h1 className={styles.heading}>{query ? `'${query}' 검색 결과` : "태그, 유저 이름을 입력해보세요"}</h1>

      {searched && (
        <div className={styles.tabs}>
          <button
            type="button"
            className={tab === "post" ? styles.tabActive : styles.tab}
            onClick={() => setTab("post")}
          >
            게시글{!loading && <span className={styles.tabCount}>{posts.length}</span>}
          </button>
          <button
            type="button"
            className={tab === "user" ? styles.tabActive : styles.tab}
            onClick={() => setTab("user")}
          >
            유저{!loading && <span className={styles.tabCount}>{users.length}</span>}
          </button>
        </div>
      )}

      {loading ? (
        <p className={styles.empty}>검색 중...</p>
      ) : !searched ? (
        <p className={styles.empty}>상단 검색창에 태그나 닉네임을 입력해보세요.</p>
      ) : tab === "post" ? (
        posts.length === 0 ? (
          <p className={styles.empty}>일치하는 태그의 게시글이 없습니다.</p>
        ) : (
          <div className={styles.postGrid}>
            {posts.map((post) => (
              <Link key={post.postId} href={`/posts/${post.postId}`} className={styles.postCard}>
                <div className={styles.thumbnail}>
                  {post.media[0] &&
                    (post.media[0].mediaType === "VIDEO" ? (
                      <video src={post.media[0].url} muted />
                    ) : (
                      <img src={post.media[0].url} alt="" />
                    ))}
                </div>
                <div className={styles.postBody}>
                  <p className={styles.postContent}>{post.content}</p>
                  <div>
                    {post.tags.map((tag) => (
                      <span className={styles.tag} key={tag}>
                        #{tag}
                      </span>
                    ))}
                  </div>
                  <p className={styles.postMeta}>
                    {post.nickname} · 조회 {post.viewCount}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        )
      ) : users.length === 0 ? (
        <p className={styles.empty}>일치하는 닉네임의 유저가 없습니다.</p>
      ) : (
        <div className={styles.userList}>
          {users.map((user) => (
            <Link
              key={user.id}
              href={user.id === currentUserId ? "/mypage" : `/users/${user.id}`}
              className={styles.userRow}
            >
              <div
                className={styles.userAvatar}
                style={
                  user.profileImageUrl
                    ? { backgroundImage: `url(${user.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
                    : undefined
                }
              />
              <div className={styles.userInfo}>
                <p className={styles.userNickname}>{user.nickname}</p>
                {user.bio && <p className={styles.userBio}>{user.bio}</p>}
              </div>
            </Link>
          ))}
        </div>
      )}
    </main>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={null}>
      <SearchResults />
    </Suspense>
  );
}
