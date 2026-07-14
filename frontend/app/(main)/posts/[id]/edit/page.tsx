"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import api from "@/lib/api";
import MediaUploader, { type MediaItem } from "@/components/post/MediaUploader";
import type { ApiResponse } from "@/types";
import styles from "./page.module.css";

type Visibility = "public" | "subscribers" | "private";

// 프론트 표시용 값 <-> 백엔드 ContentVisibility(PUBLIC/PRIVATE/LOCKED) 매핑
const VISIBILITY_MAP: Record<Visibility, string> = {
  public: "PUBLIC",
  private: "PRIVATE",
  subscribers: "LOCKED",
};
const VISIBILITY_MAP_REVERSE: Record<string, Visibility> = {
  PUBLIC: "public",
  PRIVATE: "private",
  LOCKED: "subscribers",
};

// 백엔드 PostEntity.content 컬럼 길이(@Lob, length=800)와 맞춤
const CONTENT_MAX_LENGTH = 800;

// 백엔드 PostViewDto 와 1:1로 맞춘 응답 타입
interface PostDetailResponse {
  postId: number;
  userId: number;
  content: string;
  visibility: string;
  tags: string[];
  media: MediaItem[];
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export default function EditPostPage() {
  const router = useRouter();
  const { id: postId } = useParams<{ id: string }>();

  const [loading, setLoading] = useState(true);
  const [visibility, setVisibility] = useState<Visibility>("public");
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [submitting, setSubmitting] = useState(false);

  // 기존 글 값 불러와서 폼 채우기. media도 여기서 그대로 value로 넘겨받아야
  // 저장할 때 "지우고 다시 채우기" 방식에서 기존 사진이 안 날아감.
  useEffect(() => {
    api
      .get<ApiResponse<PostDetailResponse>>(`/api/v1/posts/${postId}`, {
        headers: { "X-User-Id": 1 },
      })
      .then((res) => {
        const post = res.data.data;
        setContent(post.content ?? "");
        setVisibility(VISIBILITY_MAP_REVERSE[post.visibility] ?? "public");
        setTagsInput(post.tags.map((tag) => `#${tag}`).join(""));
        setMedia(post.media ?? []);
      })
      .catch(() => alert("게시글을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [postId]);

  // TODO: 로그인(JWT) 붙으면 X-User-Id 대신 Authorization 토큰 기반으로 전환
  const handleSubmit = async () => {
    if (!content.trim() && media.length === 0) {
      alert("내용을 입력하거나 파일을 첨부해주세요.");
      return;
    }
    const tags = tagsInput
      .split("#")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    setSubmitting(true);
    try {
      await api.put(
        `/api/v1/posts/${postId}`,
        { content, visibility: VISIBILITY_MAP[visibility], tags, media },
        { headers: { "X-User-Id": 1 } }
      );
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
          </div>
        </div>

        <div>
          <div data-panel="settings">
            <h3>
              <span data-icon data-tone="primary">visibility</span>
              공개 설정
            </h3>

            <label>
              <input type="radio" name="visibility" checked={visibility === "public"} onChange={() => setVisibility("public")} />
              <div>
                <p>전체 공개</p>
                <p>누구나 볼 수 있습니다.</p>
              </div>
            </label>

            <label>
              <input type="radio" name="visibility" checked={visibility === "subscribers"} onChange={() => setVisibility("subscribers")} />
              <div>
                <p>구독자 전용</p>
                <p>선택한 등급의 구독자만 가능.</p>
              </div>
            </label>

            <label>
              <input type="radio" name="visibility" checked={visibility === "private"} onChange={() => setVisibility("private")} />
              <div>
                <p>나만 보기</p>
                <p>비공개로 저장됩니다.</p>
              </div>
            </label>

            {visibility === "subscribers" && (
              <select defaultValue="basic">
                <option value="basic">Tier 1 (베이직)</option>
                <option value="premium">Tier 2 (프리미엄)</option>
                <option value="vip">Tier 3 (VIP)</option>
              </select>
            )}
          </div>

          <button data-action="submit" disabled={submitting} onClick={handleSubmit} type="button">
            <span data-icon data-filled="true">save</span>
            {submitting ? "수정 중..." : "수정 완료"}
          </button>
        </div>
      </div>
    </div>
  );
}
