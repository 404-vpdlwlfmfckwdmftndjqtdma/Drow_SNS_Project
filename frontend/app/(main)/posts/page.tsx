"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { AUTH_CHANGE_EVENT, getCurrentUserId } from "@/lib/auth";
import CommentButton from "@/components/comment/CommentButton";
import PostLikeButton from "@/components/post/PostLikeButton";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostViewDto 와 1:1로 맞춘 응답 타입. PostSummary(title 기준)는 아직 안 맞아서 안 씀.
interface PostListItem {
  postId: number;
  userId: number;
  nickname: string;
  profileImageUrl?: string;
  content: string;
  likeCount?: number;
  commentCount?: number;
  visibility: string;
  tags: string[];
  media: { url: string; mediaType: "IMAGE" | "VIDEO" }[];
  viewCount: number;
  createdAt: string;
}

// 이 글자수 넘어가면 "더보기"로 접어서 보여줌 (카드 하나 높이가 들쭉날쭉해지는 것 방지)
const CONTENT_PREVIEW_LIMIT = 120;

// 썸네일 그리드에 한 번에 보여줄 최대 장수. 이거보다 많으면 마지막 칸에 "+N" 오버레이.
const MAX_THUMBNAIL_GRID_ITEMS = 4;

function Thumbnail({ media }: { media: PostListItem["media"] }) {
  if (media.length === 0) return null;

  if (media.length === 1) {
    const item = media[0];
    return (
      <div className={styles.thumbnail}>
        {item.mediaType === "VIDEO" ? <video src={item.url} muted /> : <img src={item.url} alt="" />}
      </div>
    );
  }

  const visible = media.slice(0, MAX_THUMBNAIL_GRID_ITEMS);
  const extraCount = media.length - visible.length;

  return (
    <div className={`${styles.thumbnail} ${styles.thumbnailGrid} ${styles[`grid${visible.length}`]}`}>
      {visible.map((item, i) => (
        <div className={styles.gridCell} key={i}>
          {item.mediaType === "VIDEO" ? <video src={item.url} muted /> : <img src={item.url} alt="" />}
          {i === visible.length - 1 && extraCount > 0 && (
            <div className={styles.gridMoreOverlay}>+{extraCount}</div>
          )}
        </div>
      ))}
    </div>
  );
}

export default function PostListPage() {
  const [posts, setPosts] = useState<PostListItem[]>([]);
  const [error, setError] = useState("");
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

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

  useEffect(() => {
    api
      .get<ApiResponse<PostListItem[]>>("/api/v1/posts")
      .then((res) => setPosts(res.data.data))
      .catch(() => setError("목록을 불러오지 못했습니다."));
  }, []);

  // 카드 전체가 상세페이지로 가는 링크라서, 더보기 버튼 클릭이 그 링크 이동으로 안 번지게 막아야 함
  const toggleExpand = (event: React.MouseEvent, postId: number) => {
    event.preventDefault();
    event.stopPropagation();
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(postId)) {
        next.delete(postId);
      } else {
        next.add(postId);
      }
      return next;
    });
  };

  return (
    <main className={styles.container}>
      <h1 className={styles.title}>피드</h1>

      <div className={styles.filters}>{/* TODO: 콘텐츠 타입/채널/태그 필터, 정렬 셀렉트 */}</div>

      {error && <p>{error}</p>}

      <div className={styles.grid}>
        {posts.map((post) => {
          const isExpanded = expandedIds.has(post.postId);
          const isLong = post.content.length > CONTENT_PREVIEW_LIMIT;
          const shownContent = isLong && !isExpanded
            ? `${post.content.slice(0, CONTENT_PREVIEW_LIMIT)}…`
            : post.content;

          return (
            <article className={styles.card} key={post.postId}>
              <Link className={styles.cardHeader} href={`/users/${post.userId}`}>
                <div
                  className={styles.avatar}
                  style={
                    post.profileImageUrl
                      ? { backgroundImage: `url(${post.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
                      : undefined
                  }
                />
                <div>
                  <p className={styles.authorName}>{post.nickname ?? `작성자 #${post.userId}`}</p>
                  <p className={styles.timestamp}>{new Date(post.createdAt).toLocaleString()}</p>
                </div>
              </Link>

              <Link className={styles.cardMain} href={`/posts/${post.postId}`}>
              <Thumbnail media={post.media} />

              <div className={styles.body}>
                <p className={styles.content}>
                  {shownContent}
                  {isLong && (
                    <button
                      className={styles.moreButton}
                      onClick={(event) => toggleExpand(event, post.postId)}
                      type="button"
                    >
                      {isExpanded ? " 접기" : " 더보기"}
                    </button>
                  )}
                </p>

                <div>
                  {post.tags.map((tag) => (
                    <span className={styles.tag} key={tag}>
                      #{tag}
                    </span>
                  ))}
                </div>

              </div>
              </Link>

              <div className={styles.actionRow}>
                <PostLikeButton
                  postId={post.postId}
                  userId={currentUserId}
                  initialLikeCount={post.likeCount ?? 0}
                />
                <CommentButton
                  postId={post.postId}
                  userId={currentUserId}
                  initialCommentCount={post.commentCount}
                />
                <span className={styles.viewCount}>조회 {post.viewCount}</span>
              </div>
            </article>
          );
        })}
      </div>
    </main>
  );
}
