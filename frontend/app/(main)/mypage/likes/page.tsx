"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { AUTH_CHANGE_EVENT, getCurrentUserId } from "@/lib/auth";
import CommentButton from "@/components/comment/CommentButton";
import PostLikeButton from "@/components/post/PostLikeButton";
import type { ApiResponse } from "@/types";
import styles from "../account-list.module.css";

interface PostListItem {
  postId: number;
  userId: number;
  nickname: string;
  content: string;
  likeCount?: number;
  commentCount?: number;
  tags: string[];
  media: { url: string; mediaType: "IMAGE" | "VIDEO" }[];
  viewCount: number;
  createdAt: string;
}

const CONTENT_PREVIEW_LIMIT = 120;

export default function MyLikesPage() {
  const [posts, setPosts] = useState<PostListItem[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
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
    if (currentUserId == null) {
      setPosts([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");

    api
      .get<ApiResponse<PostListItem[]>>("/api/v1/likes/me/posts")
      .then((res) => setPosts(res.data.data))
      .catch(() => setError("좋아요한 게시글을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [currentUserId]);

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
      <header className={styles.header}>
        <p className={styles.eyebrow}>MY ACTIVITY</p>
        <h1 className={styles.title}>좋아요</h1>
        <p className={styles.description}>내가 좋아요한 게시물을 한곳에서 확인합니다.</p>
      </header>

      {error && <p className={styles.error}>{error}</p>}

      {currentUserId == null ? (
        <section className={styles.emptyState}>
          <span className={styles.iconWrap} aria-hidden="true">
            <span className="material-symbols-outlined">favorite</span>
          </span>
          <h2 className={styles.emptyTitle}>로그인 후 확인할 수 있어요</h2>
          <p className={styles.emptyDescription}>내가 좋아요한 게시글은 로그인한 계정에서만 조회됩니다.</p>
        </section>
      ) : loading ? (
        <section className={styles.emptyState}>
          <h2 className={styles.emptyTitle}>불러오는 중...</h2>
        </section>
      ) : posts.length === 0 ? (
        <section className={styles.emptyState}>
          <span className={styles.iconWrap} aria-hidden="true">
            <span className="material-symbols-outlined">favorite</span>
          </span>
          <h2 className={styles.emptyTitle}>좋아요한 게시글이 없습니다</h2>
          <p className={styles.emptyDescription}>마음에 드는 게시글에 좋아요를 누르면 여기에 모아볼 수 있어요.</p>
        </section>
      ) : (
        <section className={styles.activityGrid}>
          {posts.map((post) => {
            const isExpanded = expandedIds.has(post.postId);
            const isLong = post.content.length > CONTENT_PREVIEW_LIMIT;
            const shownContent = isLong && !isExpanded
              ? `${post.content.slice(0, CONTENT_PREVIEW_LIMIT)}...`
              : post.content;

            return (
              <article className={styles.activityCard} key={post.postId}>
                <Link className={styles.cardMain} href={`/posts/${post.postId}`}>
                  <div className={styles.cardHeader}>
                    <div className={styles.avatar} />
                    <div>
                      <p className={styles.authorName}>{post.nickname}</p>
                      <p className={styles.timestamp}>{new Date(post.createdAt).toLocaleString()}</p>
                    </div>
                  </div>

                  {post.media.length > 0 && (
                    <div className={styles.thumbnail}>
                      {post.media[0].mediaType === "VIDEO" ? (
                        <video src={post.media[0].url} muted />
                      ) : (
                        <img src={post.media[0].url} alt="" />
                      )}
                    </div>
                  )}

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
        </section>
      )}
    </main>
  );
}
