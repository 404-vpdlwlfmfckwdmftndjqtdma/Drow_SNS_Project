"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import styles from "./page.module.css";

type PostType = "text" | "image" | "video";
type Visibility = "public" | "subscribers" | "private";

// 프론트 표시용 값 -> 백엔드 ContentVisibility(PUBLIC/PRIVATE/LOCKED) 매핑
const VISIBILITY_MAP: Record<Visibility, string> = {
  public: "PUBLIC",
  private: "PRIVATE",
  subscribers: "LOCKED",
};

// 게시글 작성. TODO: MediaUploader 연동(lib/image.ts, 이미지/영상 첨부)은 아직 미구현 — 지금은 텍스트만 전송
export default function NewPostPage() {
  const router = useRouter();
  const [postType, setPostType] = useState<PostType>("text");
  const [visibility, setVisibility] = useState<Visibility>("public");
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // TODO: 로그인(JWT) 붙으면 X-User-Id 대신 Authorization 토큰 기반으로 전환
  const handleSubmit = async () => {
    if (!content.trim()) {
      alert("내용을 입력해주세요.");
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
        { content, visibility: VISIBILITY_MAP[visibility], tags },
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
          <div data-panel="type-selector">
            <button
              data-active={postType === "text" ? "true" : undefined}
              onClick={() => setPostType("text")}
              type="button"
            >
              <span data-icon>notes</span>
              텍스트
            </button>
            <button
              data-active={postType === "image" ? "true" : undefined}
              onClick={() => setPostType("image")}
              type="button"
            >
              <span data-icon>image</span>
              이미지
            </button>
            <button
              data-active={postType === "video" ? "true" : undefined}
              onClick={() => setPostType("video")}
              type="button"
            >
              <span data-icon>videocam</span>
              비디오
            </button>
          </div>

          <div data-panel="editor">
            <textarea
              onChange={(event) => setContent(event.target.value)}
              placeholder="무슨 생각을 하고 계신가요? 내용을 입력하세요..."
              value={content}
            />

            <input
              onChange={(event) => setTagsInput(event.target.value)}
              placeholder="태그를 #으로 구분해서 입력하세요 (예: #일상#그림)"
              type="text"
              value={tagsInput}
            />

            {postType !== "text" && (
              <div data-panel="upload">
                {/* TODO: components/post/MediaUploader 로 교체 (lib/image.ts uploadMediaBatch 사용) */}
                <span data-icon data-size="large">cloud_upload</span>
                <p>파일을 드래그하거나 클릭하여 업로드</p>
                <p>최대 100MB (JPG, PNG, MP4 등 지원)</p>
              </div>
            )}

            <div data-toolbar>
              <div>
                <button type="button"><span data-icon>mood</span></button>
                <button type="button"><span data-icon>location_on</span></button>
                <button type="button"><span data-icon>alternate_email</span></button>
                <button type="button"><span data-icon>tag</span></button>
              </div>
              <span>{content.length} / 2000 자</span>
            </div>
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

          <div data-panel="tip">
            <p>
              <span data-icon>lightbulb</span>
              작성 팁
            </p>
            <p>
              구독자 전용 게시물은 일반 게시물보다 <strong>2.5배</strong> 더 높은 참여도를 보입니다. 전용 혜택을 강조해보세요!
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
