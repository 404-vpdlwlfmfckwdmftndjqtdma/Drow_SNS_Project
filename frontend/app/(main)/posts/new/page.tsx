"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import MediaUploader, { type MediaItem } from "@/components/post/MediaUploader";
import type { BlurRange } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostEntity.content 컬럼 길이(@Lob, length=800)와 맞춤
const CONTENT_MAX_LENGTH = 800;

// 공개 범위 선택은 두지 않는다.
// 접근 제어는 전적으로 블러(가린 부분) + 가격으로 표현한다 - 구독자는 블러가 전부 풀리고,
// 비구독자는 가격이 매겨진 부분만 개별 구매할 수 있다.
// (예전엔 "전체 공개 / 구독자 전용" 라디오와 등급 드롭다운이 있었지만, 고른 값이 서버로
//  가지도 않고 백엔드 판정에도 쓰이지 않는 장식이었다.)
export default function NewPostPage() {
  const router = useRouter();
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [blurRanges, setBlurRanges] = useState<BlurRange[]>([]);
  const [blurredImageIndexes, setBlurredImageIndexes] = useState<Set<number>>(new Set());
  // 가린 부분을 얼마에 팔지. 비워두면 판매하지 않는다(구독자만 열람 가능).
  const [textBlurPrice, setTextBlurPrice] = useState("");
  const [imageBlurPrice, setImageBlurPrice] = useState("");
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

    // 가격표: 실제로 블러를 건 기능에만 값을 매긴다. 0원이거나 비워두면 판매하지 않는다
    // (= 구독자만 볼 수 있는 글). 금액은 서버가 이 값을 저장해 두고 구매 시 직접 조회한다.
    const prices: Record<string, number> = {};
    if (blurRanges.length > 0 && Number(textBlurPrice) > 0) {
      prices.textBlur = Number(textBlurPrice);
    }
    if (blurredImageIndexes.size > 0 && Number(imageBlurPrice) > 0) {
      prices.imageBlur = Number(imageBlurPrice);
    }

    setSubmitting(true);
    try {
      // visibility는 보내지 않는다 - 서버가 PUBLIC으로 저장한다
      await api.post("/api/v1/posts", {
        content,
        tags,
        media,
        extensions: Object.keys(extensions).length > 0 ? extensions : undefined,
        prices: Object.keys(prices).length > 0 ? prices : undefined,
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
          {/* 가린 부분을 개별 판매할지 정한다. 구독자는 어차피 전부 볼 수 있고,
              여기 값은 "구독 안 한 사람이 이 글만 사서 보는" 가격이다. */}
          {(blurRanges.length > 0 || blurredImageIndexes.size > 0) && (
            <div data-panel="settings">
              <h3>
                <span data-icon data-tone="primary">sell</span>
                가린 부분 판매
              </h3>

              {blurRanges.length > 0 && (
                <label className={styles.priceField}>
                  <span>텍스트 블러 해제</span>
                  <input
                    type="number"
                    min={0}
                    step={1000}
                    placeholder="비우면 판매 안 함"
                    value={textBlurPrice}
                    onChange={(e) => setTextBlurPrice(e.target.value)}
                  />
                  <span>원</span>
                </label>
              )}

              {blurredImageIndexes.size > 0 && (
                <label className={styles.priceField}>
                  <span>이미지 블러 해제</span>
                  <input
                    type="number"
                    min={0}
                    step={1000}
                    placeholder="비우면 판매 안 함"
                    value={imageBlurPrice}
                    onChange={(e) => setImageBlurPrice(e.target.value)}
                  />
                  <span>원</span>
                </label>
              )}

              <p className={styles.priceHint}>
                비워두면 판매하지 않습니다. 이 경우 구독자만 가린 부분을 볼 수 있습니다.
              </p>
            </div>
          )}

          <button data-action="submit" disabled={submitting} onClick={handleSubmit} type="button">
            <span data-icon data-filled="true">send</span>
            {submitting ? "게시 중..." : "게시하기"}
          </button>
        </div>
      </div>
    </div>
  );
}
