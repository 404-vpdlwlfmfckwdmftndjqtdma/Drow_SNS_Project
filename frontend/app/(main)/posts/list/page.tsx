"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";

interface PostItem {
  id: number;
  text: string;
}

export default function PostListPage() {
  const [posts, setPosts] = useState<PostItem[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get<PostItem[]>("/api/v1/post")
      .then((res) => setPosts(res.data))
      .catch(() => setError("목록을 불러오지 못했습니다."));
  }, []);

  if (error) {
    return <main>{error}</main>;
  }

  return (
    <main>
      <h1>게시글 목록</h1>
      {posts.length === 0 ? (
        <p>게시글이 없습니다.</p>
      ) : (
        <ul>
          {posts.map((post) => (
            <li key={post.id}>
              #{post.id} — {post.text}
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
