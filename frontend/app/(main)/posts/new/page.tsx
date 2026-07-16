"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import MediaUploader, { type MediaItem } from "@/components/post/MediaUploader";
import type { BlurRange } from "@/types";
import styles from "./page.module.css";

type Visibility = "public" | "subscribers";

// 프론트 표시용 값 -> 백엔드 ContentVisibility(PUBLIC/LOCKED) 매핑
const VISIBILITY_MAP: Record<Visibility, string> = {
  public: "PUBLIC",
  subscribers: "LOCKED",
};

// 백엔드 PostEntity.content 컬럼 길이(@Lob, length=800)와 맞춤
const CONTENT_MAX_LENGTH = 800;

export default function NewPostPage() {
  const router = useRouter();
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [visibility, setVisibility] = useState<Visibility>("public");
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [blurRanges, setBlurRanges] = useState<BlurRange[]>([]);
  const [blurredImageIndexes, setBlurredImageIndexes] = useState<Set<number>>(new Set());
  const [submitting, setSubmitting] = useState(false);

  // 사진이 지워지면 뒤 사진들 인덱스가 앞으로 밀리므로, 안전하게 블러 선택을 초기화한다.
  const handleMediaChange = (next: MediaItem[]) => {
    if (next.length < media.length) {
      setBlurredImageIndexes(new Set());
    }
    setMedia(next);
  };

  const handleToggleImageBlur = (index: number) => {
    setBlurredImageIndexes((prev) => {
      const next = new Set(prev);
      if (next.has(index)) {
        next.delete(index);
      } else {
        next.add(index);
      }
      return next;
    });
  };

  // 선택 영역 -> 블러 처리 목록에 추가. textarea의 selectionStart/End가 곧 글자 위치(인덱스)라
  // 백엔드 TextBlurRange가 원하는 {start, end} 그대로 쓸 수 있다.
  const handleAddBlurRange = () => {
    const textarea = textareaRef.current;
    if (!textarea) return;
    const { selectionStart: start, selectionEnd: end } = textarea;
    if (start === end) {
      alert("블러 처리할 텍스트를 먼저 드래그해서 선택해주세요.");
      return;
    }
    setBlurRanges((prev) => [...prev, { start, end }]);
  };

  const handleRemoveBlurRange = (index: number) => {
    setBlurRanges((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (!content.trim() && media.length === 0) {
      alert("내용을 입력하거나 파일을 첨부해주세요.");
      return;
    }
    const tags = tagsInput
      .split("#")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    const extensions: Record<string, unknown> = {};
    if (blurRanges.length > 0) {
      extensions.textBlur = { ranges: blurRanges };
    }
    if (blurredImageIndexes.size > 0) {
      extensions.imageBlur = { targetIndexes: Array.from(blurredImageIndexes) };
    }

    setSubmitting(true);
    try {
      await api.post("/api/v1/posts", {
        content,
        visibility: VISIBILITY_MAP[visibility],
        tags,
        media,
        extensions: Object.keys(extensions).length > 0 ? extensions : undefined,
      });
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
            <MediaUploader
              value={media}
              onChange={handleMediaChange}
              blurredIndexes={blurredImageIndexes}
              onToggleBlur={handleToggleImageBlur}
            />

            <textarea
              ref={textareaRef}
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

          <button className={styles.blurButton} onClick={handleAddBlurRange} type="button">
            <span className="material-symbols-outlined">visibility_off</span>
            선택한 텍스트 블러 처리
          </button>

          {blurRanges.length > 0 && (
            <ul className={styles.blurList}>
              {blurRanges.map((range, index) => (
                <li key={`${range.start}-${range.end}-${index}`}>
                  <span>“{content.slice(range.start, range.end)}”</span>
                  <button onClick={() => handleRemoveBlurRange(index)} type="button">
                    ✕
                  </button>
                </li>
              ))}
            </ul>
          )}
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
