"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostViewDto 와 1:1로 맞춘 응답 타입. PostSummary(title 기준)는 아직 안 맞아서 안 씀.
interface PostListItem {
  postId: number;
  userId: number;
  content: string;
  visibility: string;
  tags: string[];
  viewCount: number;
  createdAt: string;
}

export default function PostListPage() {
  const [posts, setPosts] = useState<PostListItem[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get<ApiResponse<PostListItem[]>>("/api/v1/posts")
      .then((res) => setPosts(res.data.data))
      .catch(() => setError("목록을 불러오지 못했습니다."));
  }, []);

  return (
    <main className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>게시글</h1>
      </div>

      <div className={styles.filters}>{/* TODO: 콘텐츠 타입/채널/태그 필터, 정렬 셀렉트 */}</div>

      {error && <p>{error}</p>}

      <div className={styles.grid}>
        {posts.map((post) => (
          <article className={styles.card} key={post.postId}>
            <p>{post.content}</p>
            <div>
              {post.tags.map((tag) => (
                <span className={styles.tag} key={tag}>
                  #{tag}
                </span>
              ))}
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}
