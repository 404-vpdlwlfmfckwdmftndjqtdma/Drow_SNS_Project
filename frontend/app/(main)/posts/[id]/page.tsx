"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/api";
import { AUTH_CHANGE_EVENT, getCurrentUserId } from "@/lib/auth";
import CommentThread from "@/components/comment/CommentThread";
import PurchaseButton from "@/components/payment/PurchaseButton";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostViewDto 와 1:1로 맞춘 응답 타입
interface PostDetailResponse {
  postId: number;
  userId: number;
  nickname: string;
  profileImageUrl?: string;
  content: string;
  visibility: "PUBLIC" | "LOCKED";
  tags: string[];
  media: { url: string; mediaType: "IMAGE" | "VIDEO" }[];
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

const VISIBILITY_LABEL: Record<string, string> = {
  PUBLIC: "전체 공개",
  LOCKED: "구독자 전용",
};

const CAPABILITY_LABEL: Record<"textBlur" | "imageBlur", string> = {
  textBlur: "텍스트 블러 해제",
  imageBlur: "이미지 블러 해제",
};

// ImageBlurExtension이 블러 걸 때 URL에 이 문자열을 끼워넣는다 (백엔드 posts/imageblur 참고).
// 원본/블러본을 서버가 URL 자체로 구분해서 내려주기 때문에, 프론트는 URL만 보고 판단하면 된다.
const BLUR_URL_MARKER = "/upload/e_blur:";

function isBlurredImageUrl(url: string) {
  return url.includes(BLUR_URL_MARKER);
}

// 백엔드가 블러 구간을 ● 로 치환해서 보내주므로(TextBlurExtension), 연속된 ● 구간만 클릭 가능하게 감싼다.
function renderContentWithBlurClick(content: string, onBlurredClick: () => void) {
  return content.split(/(●+)/g).map((part, index) =>
    part.startsWith("●") ? (
      <button
        key={index}
        type="button"
        className={styles.blurredTextSpan}
        onClick={onBlurredClick}
      >
        {part}
      </button>
    ) : (
      <span key={index}>{part}</span>
    )
  );
}

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
  const [purchaseCapability, setPurchaseCapability] = useState<"textBlur" | "imageBlur" | null>(null);

  const openPurchase = (capability: "textBlur" | "imageBlur") => {
    setPurchaseCapability(capability);
  };

  const fetchPost = () => {
    api
      .get<ApiResponse<PostDetailResponse>>(`/api/v1/posts/${postId}`)
      .then((res) => setPost(res.data.data))
      .catch(() => setError("게시글을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  };

  // 구매 완료 시 호출 - 잠금 해제된 내용으로 다시 불러오고 모달을 닫는다.
  const handlePurchaseDone = () => {
    setPurchaseCapability(null);
    fetchPost();
  };

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
    setMediaIndex(0);
    setLoading(true);
    fetchPost();
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
      <button className={styles.backButton} onClick={() => router.back()} type="button">
        <span className="material-symbols-outlined">arrow_back</span>
        뒤로가기
      </button>

      <article className={styles.card}>
        <div className={styles.cardInner}>
          <div className={styles.header}>
            <Link href={`/users/${post.userId}`} className={styles.authorLink}>
              <div
                className={styles.avatar}
                style={
                  post.profileImageUrl
                    ? { backgroundImage: `url(${post.profileImageUrl})`, backgroundSize: "cover", backgroundPosition: "center" }
                    : undefined
                }
              />
              <div className={styles.headerText}>
                <p className={styles.authorName}>{post.nickname ?? `작성자 #${post.userId}`}</p>
                <p className={styles.timestamp}>{new Date(post.createdAt).toLocaleString()}</p>
              </div>
            </Link>
            <span className={styles.visibilityTag}>{VISIBILITY_LABEL[post.visibility]}</span>
          </div>

          {post.media.length > 0 && (
            <section className={styles.mediaCarousel}>
              <div className={styles.mediaItem}>
                {post.media[mediaIndex].mediaType === "VIDEO" ? (
                  <video src={post.media[mediaIndex].url} controls controlsList="nodownload" />
                ) : isBlurredImageUrl(post.media[mediaIndex].url) ? (
                  <button
                    type="button"
                    className={styles.blurredImageButton}
                    onClick={() => openPurchase("imageBlur")}
                  >
                    <img src={post.media[mediaIndex].url} alt="" />
                  </button>
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

          {post.content && (
            <p className={styles.content}>
              {renderContentWithBlurClick(post.content, () => openPurchase("textBlur"))}
            </p>
          )}

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
        <CommentThread postId={post.postId} userId={currentUserId} />
      </section>

      {purchaseCapability && (
        <div className={styles.purchaseOverlay} onClick={() => setPurchaseCapability(null)}>
          <div className={styles.purchaseModal} onClick={(event) => event.stopPropagation()}>
            <button
              type="button"
              className={styles.purchaseModalClose}
              onClick={() => setPurchaseCapability(null)}
            >
              <span className="material-symbols-outlined">close</span>
            </button>
            <PurchaseButton
              postId={post.postId}
              capability={purchaseCapability}
              label={CAPABILITY_LABEL[purchaseCapability]}
              onDone={handlePurchaseDone}
            />
          </div>
        </div>
      )}
    </div>
  );
}
