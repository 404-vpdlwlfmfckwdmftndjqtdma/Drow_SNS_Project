"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import api from "@/lib/api";
import CommentForm from "@/components/comment/CommentForm";
import CommentList from "@/components/comment/CommentList";
import type { ApiResponse, User } from "@/types";
import styles from "./page.module.css";

interface CommentItem {
  id: number;
  content: string;
  writerId: number;
  writerNickname?: string;
}

// 백엔드 PostViewDto 와 1:1로 맞춘 응답 타입
interface PostDetailResponse {
  postId: number;
  userId: number;
  content: string;
  visibility: "PUBLIC" | "PRIVATE" | "LOCKED";
  tags: string[];
  media: { url: string; mediaType: "IMAGE" | "VIDEO" }[];
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

const VISIBILITY_LABEL: Record<string, string> = {
  PUBLIC: "전체 공개",
  LOCKED: "구독자 전용",
  PRIVATE: "나만 보기",
};

// 게시글 상세. 구독 잠금(LOCKED) 열람 제한은 아직 서버에 연동 전이라(subscription 도메인 연동 대기 중)
// 지금은 공개 범위 뱃지만 보여주고, 실제 블러/잠금 UI는 나중에 붙인다.
export default function PostDetailPage() {
  const router = useRouter();
  const { id: postId } = useParams<{ id: string }>();

  const [post, setPost] = useState<PostDetailResponse | null>(null);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mediaIndex, setMediaIndex] = useState(0);
  const [comments, setComments] = useState<CommentItem[]>([]);

  useEffect(() => {
    api
      .get<ApiResponse<User>>("/api/v1/users/me")
      .then((res) => setCurrentUserId(res.data.data.id))
      .catch(() => setCurrentUserId(null));
  }, []);

  useEffect(() => {
    setMediaIndex(0);
    api
      .get<ApiResponse<PostDetailResponse>>(`/api/v1/posts/${postId}`)
      .then((res) => setPost(res.data.data))
      .catch(() => setError("게시글을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [postId]);

  const fetchComments = async () => {
    try {
      const res = await api.get<ApiResponse<{ content: CommentItem[] }>>(`/api/v1/posts/${postId}/comments`, {
        params: { size: 50 },
      });
      setComments(res.data.data.content);
    } catch {
      setError("댓글을 불러오지 못했습니다.");
    }
  };

  useEffect(() => {
    if (!postId) return;
    fetchComments();
  }, [postId]);

  const handleDelete = async () => {
    if (!confirm("이 게시글을 삭제하시겠어요?")) return;
    try {
      await api.delete(`/api/v1/posts/${postId}`);
      router.push("/posts");
    } catch {
      alert("삭제에 실패했습니다.");
    }
  };

  if (loading) {
    return <div className={styles.container}>불러오는 중...</div>;
  }

  if (error || !post) {
    return <div className={styles.container}>{error || "게시글을 찾을 수 없습니다."}</div>;
  }

  const isOwner = currentUserId !== null && post.userId === currentUserId;

  return (
    <div className={styles.container}>
      <article className={styles.card}>
        <div className={styles.cardInner}>
          <div className={styles.header}>
            <div className={styles.avatar} />
            <div className={styles.headerText}>
              <p className={styles.authorName}>작성자 #{post.userId}</p>
              <p className={styles.timestamp}>{new Date(post.createdAt).toLocaleString()}</p>
            </div>
            <span className={styles.visibilityTag}>{VISIBILITY_LABEL[post.visibility]}</span>
          </div>

          {post.media.length > 0 && (
            <section className={styles.mediaCarousel}>
              <div className={styles.mediaItem}>
                {post.media[mediaIndex].mediaType === "VIDEO" ? (
                  <video src={post.media[mediaIndex].url} controls controlsList="nodownload" />
                ) : (
                  <img src={post.media[mediaIndex].url} alt="" />
                )}
              </div>

              {post.media.length > 1 && (
                <>
                  <button
                    className={`${styles.carouselArrow} ${styles.carouselArrowLeft}`}
                    disabled={mediaIndex === 0}
                    onClick={() => setMediaIndex((i) => i - 1)}
                    type="button"
                  >
                    <span className="material-symbols-outlined">chevron_left</span>
                  </button>
                  <button
                    className={`${styles.carouselArrow} ${styles.carouselArrowRight}`}
                    disabled={mediaIndex === post.media.length - 1}
                    onClick={() => setMediaIndex((i) => i + 1)}
                    type="button"
                  >
                    <span className="material-symbols-outlined">chevron_right</span>
                  </button>
                  <span className={styles.carouselCount}>
                    {mediaIndex + 1} / {post.media.length}
                  </span>
                </>
              )}
            </section>
          )}

          {post.content && <p className={styles.content}>{post.content}</p>}

          {post.tags.length > 0 && (
            <div className={styles.tags}>
              {post.tags.map((tag) => (
                <span key={tag}>#{tag}</span>
              ))}
            </div>
          )}

          <div className={styles.footer}>
            {isOwner && (
              <div className={styles.actions}>
                <button type="button" onClick={() => router.push(`/posts/${postId}/edit`)}>
                  수정
                </button>
                <button type="button" onClick={handleDelete}>
                  삭제
                </button>
              </div>
            )}
            <p className={styles.meta}>조회 {post.viewCount}</p>
          </div>
        </div>
      </article>

      <section className={styles.commentSection}>
        <h2 className={styles.commentTitle}>댓글</h2>
        <CommentForm postId={post.postId} onSubmitted={fetchComments} />
        <CommentList comments={comments} />
      </section>
    </div>
  );
}
