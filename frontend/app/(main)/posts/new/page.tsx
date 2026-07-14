"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import MediaUploader, { type MediaItem } from "@/components/post/MediaUploader";
import styles from "./page.module.css";

type Visibility = "public" | "subscribers" | "private";

// 프론트 표시용 값 -> 백엔드 ContentVisibility(PUBLIC/PRIVATE/LOCKED) 매핑
const VISIBILITY_MAP: Record<Visibility, string> = {
  public: "PUBLIC",
  private: "PRIVATE",
  subscribers: "LOCKED",
};

// 백엔드 PostEntity.content 컬럼 길이(@Lob, length=800)와 맞춤
const CONTENT_MAX_LENGTH = 800;

export default function NewPostPage() {
  const router = useRouter();
  const [visibility, setVisibility] = useState<Visibility>("public");
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [submitting, setSubmitting] = useState(false);

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
      await api.post(
        "/api/v1/posts",
        { content, visibility: VISIBILITY_MAP[visibility], tags, media },
        { headers: { "X-User-Id": 1 } }
      );
      router.push("/posts");
    } catch {
      alert("게시글 등록에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

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
            <span data-icon data-filled="true">send</span>
            {submitting ? "게시 중..." : "게시하기"}
          </button>
        </div>
      </div>
    </div>
  );
}
