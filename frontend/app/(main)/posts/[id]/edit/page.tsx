"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import api from "@/lib/api";
import MediaUploader, { type MediaItem } from "@/components/post/MediaUploader";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostEntity.content 컬럼 길이(@Lob, length=800)와 맞춤
const CONTENT_MAX_LENGTH = 800;

// 백엔드 PostViewDto 와 1:1로 맞춘 응답 타입
interface PostDetailResponse {
  postId: number;
  userId: number;
  content: string;
  tags: string[];
  media: MediaItem[];
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

// 공개 범위 선택은 두지 않는다 (작성 화면과 동일) - 접근 제어는 블러 + 가격으로만 표현한다.
export default function EditPostPage() {
  const router = useRouter();
  const { id: postId } = useParams<{ id: string }>();

  const [loading, setLoading] = useState(true);
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [textBlurPrice, setTextBlurPrice] = useState("");
  const [imageBlurPrice, setImageBlurPrice] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // 기존 글 값 불러와서 폼 채우기. media도 여기서 그대로 value로 넘겨받아야
  // 저장할 때 지우고 다시 채우기 방식에서 기존 사진이 안 날아감.
  useEffect(() => {
    api
      .get<ApiResponse<PostDetailResponse>>(`/api/v1/posts/${postId}`)
      .then((res) => {
        const post = res.data.data;
        setContent(post.content ?? "");
        setTagsInput(post.tags.map((tag) => `#${tag}`).join(""));
        setMedia(post.media ?? []);
      })
      .catch(() => alert("게시글을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [postId]);

  const handleSubmit = async () => {
    if (!content.trim() && media.length === 0) {
      alert("내용을 입력하거나 파일을 첨부해주세요.");
      return;
    }
    const tags = tagsInput
      .split("#")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    // 0원 이하/빈 값은 안 보내서 백엔드가 "판매 안 함"으로 처리하게 둔다.
    const prices: Record<string, number> = {};
    if (Number(textBlurPrice) > 0) {
      prices.textBlur = Number(textBlurPrice);
    }
    if (Number(imageBlurPrice) > 0) {
      prices.imageBlur = Number(imageBlurPrice);
    }

    setSubmitting(true);
    try {
      await api.put(`/api/v1/posts/${postId}`, {
        content,
        tags,
        media,
        prices: Object.keys(prices).length > 0 ? prices : undefined,
      });
      router.push(`/posts/${postId}`);
    } catch {
      alert("게시글 수정에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className={styles.container}>불러오는 중...</div>;
  }

  return (
    <div className={styles.container}>
      <div data-layout="post-form">
        <div>
          <div data-panel="editor">
            <MediaUploader value={media} onChange={setMedia} />

            <textarea
              maxLength={CONTENT_MAX_LENGTH}
              onChange={(event) => setContent(event.target.value)}
              placeholder="무슨 생각을 하고 계신가요? 내용을 입력하세요..."
              value={content}
            />
            <div data-char-count>{content.length} / {CONTENT_MAX_LENGTH}자</div>

            <input
              className={styles.tagsInput}
              onChange={(event) => setTagsInput(event.target.value)}
              placeholder="태그를 #으로 구분해서 입력하세요 (예: #일상#그림)"
              type="text"
              value={tagsInput}
            />

            {/* 수정 시 블러 선택은 초기화되므로(다시 선택 필요), 가격만 별도로 입력받는다 */}
            <input
              className={styles.priceInput}
              type="number"
              min={0}
              placeholder="텍스트 블러 해제 가격 (원, 블러 있을 때만)"
              value={textBlurPrice}
              onChange={(event) => setTextBlurPrice(event.target.value)}
            />
            <input
              className={styles.priceInput}
              type="number"
              min={0}
              placeholder="이미지 블러 해제 가격 (원, 블러 있을 때만)"
              value={imageBlurPrice}
              onChange={(event) => setImageBlurPrice(event.target.value)}
            />
          </div>
        </div>

        <div>
          <button data-action="submit" disabled={submitting} onClick={handleSubmit} type="button">
            <span data-icon data-filled="true">save</span>
            {submitting ? "수정 중..." : "수정 완료"}
          </button>
        </div>
      </div>
    </div>
  );
}
