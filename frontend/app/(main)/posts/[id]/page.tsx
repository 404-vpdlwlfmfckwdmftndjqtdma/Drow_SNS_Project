"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import api from "@/lib/api";
import CommentForm from "@/components/comment/CommentForm";
import CommentList from "@/components/comment/CommentList";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

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

// TODO: 로그인(JWT) 붙으면 실제 로그인한 사용자 id로 교체
const CURRENT_USER_ID = 1;

// 게시글 상세. 구독 잠금(LOCKED) 열람 제한은 아직 서버에 연동 전이라(subscription 도메인 연동 대기 중)
// 지금은 공개 범위 뱃지만 보여주고, 실제 블러/잠금 UI는 나중에 붙인다.
export default function PostDetailPage() {
  const router = useRouter();
  const { id: postId } = useParams<{ id: string }>();

  const [post, setPost] = useState<PostDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mediaIndex, setMediaIndex] = useState(0);

  useEffect(() => {
    setMediaIndex(0);
    api
      .get<ApiResponse<PostDetailResponse>>(`/api/v1/posts/${postId}`, {
        headers: { "X-User-Id": CURRENT_USER_ID },
      })
      .then((res) => setPost(res.data.data))
      .catch(() => setError("게시글을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [postId]);

  const handleDelete = async () => {
    if (!confirm("이 게시글을 삭제하시겠어요?")) return;
    try {
      await api.delete(`/api/v1/posts/${postId}`, {
        headers: { "X-User-Id": CURRENT_USER_ID },
      });
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

  const isOwner = post.userId === CURRENT_USER_ID;

  return (
    <div className={styles.container}>
      <article className={styles.card}>
        <div className={styles.cardInner}>
          <div className={styles.header}>
            <p className={styles.byline}>
              작성자 #{post.userId} · {new Date(post.createdAt).toLocaleString()}
            </p>
            <span className={styles.visibilityTag}>{VISIBILITY_LABEL[post.visibility]}</span>
          </div>

          {post.media.length > 0 && (
            <section className={styles.mediaCarousel}>
              <div className={styles.mediaItem}>
                {post.media[mediaIndex].mediaType === "VIDEO" ? (
                  <video src={post.media[mediaIndex].url} controls />
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

      {/* 댓글: 그대로 가져다 자리만 잡음. 실제 조회/등록 연동은 동현님 마저 진행 */}
      <section className={styles.commentSection}>
        <h2 className={styles.commentTitle}>댓글</h2>
        <CommentForm postId={post.postId} />
        <CommentList comments={[]} />
      </section>
    </div>
  );
}
