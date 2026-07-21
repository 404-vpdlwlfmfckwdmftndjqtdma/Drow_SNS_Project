"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import MediaUploader, { type MediaItem } from "@/components/post/MediaUploader";
import type { BlurRange } from "@/types";
import styles from "./page.module.css";

// 백엔드 PostEntity.content 컬럼 길이(@Lob, length=800)와 맞춤
const CONTENT_MAX_LENGTH = 800;

// textarea는 부분 배경색을 못 입혀서, 뒤에 겹쳐둔 배경 레이어에 블러 구간만 <mark>로 감싸 그려준다.
function renderHighlightedContent(content: string, ranges: BlurRange[]) {
  if (ranges.length === 0) {
    return content;
  }
  const sorted = [...ranges].sort((a, b) => a.start - b.start);
  const parts: React.ReactNode[] = [];
  let cursor = 0;
  sorted.forEach((range, index) => {
    const start = Math.max(range.start, cursor);
    const end = Math.max(range.end, start);
    if (start > cursor) {
      parts.push(content.slice(cursor, start));
    }
    if (end > start) {
      parts.push(
        <mark key={index} className={styles.blurredMark}>
          {content.slice(start, end)}
        </mark>
      );
    }
    cursor = Math.max(cursor, end);
  });
  if (cursor < content.length) {
    parts.push(content.slice(cursor));
  }
  // 본문이 줄바꿈으로 끝나면 배경 레이어 높이가 한 줄 부족해져서 textarea랑 밀리므로 보정한다.
  if (content.endsWith("\n")) {
    parts.push(" ");
  }
  return parts;
}

// 공개 범위 선택은 두지 않는다.
// 접근 제어는 전적으로 블러(가린 부분) + 가격으로 표현한다 - 구독자는 블러가 전부 풀리고,
// 비구독자는 가격이 매겨진 부분만 개별 구매할 수 있다.
// (예전엔 "전체 공개 / 구독자 전용" 라디오와 등급 드롭다운이 있었지만, 고른 값이 서버로
//  가지도 않고 백엔드 판정에도 쓰이지 않는 장식이었다.)
export default function NewPostPage() {
  const router = useRouter();
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const backdropRef = useRef<HTMLDivElement>(null);
  const blurPopupRef = useRef<HTMLButtonElement>(null);
  const [content, setContent] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [blurRanges, setBlurRanges] = useState<BlurRange[]>([]);
  const [blurredImageIndexes, setBlurredImageIndexes] = useState<Set<number>>(new Set());
  // 가린 부분을 얼마에 팔지. 비워두면 판매하지 않는다(구독자만 열람 가능).
  const [textBlurPrice, setTextBlurPrice] = useState("");
  const [imageBlurPrice, setImageBlurPrice] = useState("");
  const [submitting, setSubmitting] = useState(false);
  // 텍스트를 드래그해서 선택하면 그 근처에 "블러 처리" 버튼이 뜬다 (Medium/Notion 스타일 선택 툴바)
  const [blurPopup, setBlurPopup] = useState<{ x: number; y: number } | null>(null);

  // 텍스트 영역이나 팝업 버튼 바깥을 클릭하면 팝업을 닫는다.
  useEffect(() => {
    const handleClickAway = (event: MouseEvent) => {
      const target = event.target as Node;
      if (blurPopupRef.current?.contains(target) || textareaRef.current?.contains(target)) {
        return;
      }
      setBlurPopup(null);
    };
    document.addEventListener("mousedown", handleClickAway);
    return () => document.removeEventListener("mousedown", handleClickAway);
  }, []);

  // 드래그로 텍스트를 선택하면(mouseup 시점) 그 지점 근처에 팝업을 띄운다.
  const handleTextMouseUp = (event: React.MouseEvent<HTMLTextAreaElement>) => {
    const textarea = textareaRef.current;
    if (!textarea || textarea.selectionStart === textarea.selectionEnd) {
      setBlurPopup(null);
      return;
    }
    setBlurPopup({ x: event.clientX, y: event.clientY });
  };

  // 배경 하이라이트 레이어는 textarea 안 스크롤을 따로 안 따라오니 직접 맞춰준다.
  const handleTextareaScroll = (event: React.UIEvent<HTMLTextAreaElement>) => {
    if (backdropRef.current) {
      backdropRef.current.scrollTop = event.currentTarget.scrollTop;
      backdropRef.current.scrollLeft = event.currentTarget.scrollLeft;
    }
  };

  // 드래그(선택) 없이 하이라이트된 글자 위를 그냥 클릭하면 그 블러를 해제한다.
  const handleTextareaClick = () => {
    const textarea = textareaRef.current;
    if (!textarea || textarea.selectionStart !== textarea.selectionEnd) {
      return;
    }
    const pos = textarea.selectionStart;
    const hitIndex = blurRanges.findIndex((range) => pos >= range.start && pos < range.end);
    if (hitIndex !== -1) {
      setBlurRanges((prev) => prev.filter((_, i) => i !== hitIndex));
    }
  };

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
    setBlurPopup(null);
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

    // 가격을 안 매기면 그 블러는 아예 걸지 않는다(= 원문 그대로 공개).
    // 가격표 없이 블러만 걸면 구독자 아닌 사람은 영원히 못 푸는 죽은 콘텐츠가 되기 때문.
    const extensions: Record<string, unknown> = {};
    const prices: Record<string, number> = {};
    if (blurRanges.length > 0 && Number(textBlurPrice) > 0) {
      extensions.textBlur = { ranges: blurRanges };
      prices.textBlur = Number(textBlurPrice);
    }
    if (blurredImageIndexes.size > 0 && Number(imageBlurPrice) > 0) {
      extensions.imageBlur = { targetIndexes: Array.from(blurredImageIndexes) };
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
      <div className={styles.guide}>
        <span className="material-symbols-outlined" data-icon>lightbulb</span>
        <p>텍스트를 드래그하면 옆에 뜨는 버튼으로 선택한 글자를 블러 처리할 수 있어요.</p>
        <p>사진 밑의 블러 버튼을 누르면 그 사진이 블러 처리돼요.</p>
      </div>

      <div data-layout="post-form">
        <div>
          <div data-panel="editor">
            <MediaUploader
              value={media}
              onChange={handleMediaChange}
              blurredIndexes={blurredImageIndexes}
              onToggleBlur={handleToggleImageBlur}
            />

            <div className={styles.textareaWrapper}>
              <div ref={backdropRef} className={styles.highlightBackdrop} aria-hidden="true">
                {renderHighlightedContent(content, blurRanges)}
              </div>
              <textarea
                ref={textareaRef}
                maxLength={CONTENT_MAX_LENGTH}
                onChange={(event) => setContent(event.target.value)}
                onMouseUp={handleTextMouseUp}
                onClick={handleTextareaClick}
                onScroll={handleTextareaScroll}
                placeholder="무슨 생각을 하고 계신가요? 내용을 입력하세요..."
                value={content}
              />
            </div>
            <div data-char-count>{content.length} / {CONTENT_MAX_LENGTH}자</div>

            <input
              className={styles.tagsInput}
              onChange={(event) => setTagsInput(event.target.value)}
              placeholder="태그를 #으로 구분해서 입력하세요 (예: #일상#그림)"
              type="text"
              value={tagsInput}
            />
          </div>

          {blurRanges.length > 0 && (
            <p className={styles.blurHint}>하이라이트된 글자를 클릭하면 블러가 해제돼요.</p>
          )}
        </div>

        <div>
          {(blurRanges.length > 0 || blurredImageIndexes.size > 0) && (
            <div data-panel="settings">
              <h3>
                <span data-icon data-tone="primary">sell</span>
                판매 가격
              </h3>

              {blurRanges.length > 0 && (
                <div className={styles.priceField}>
                  <label htmlFor="textBlurPrice">텍스트 블러 해제</label>
                  <input
                    id="textBlurPrice"
                    className={styles.priceInput}
                    type="number"
                    min={0}
                    placeholder="가격 (원, 비우면 판매 안 함)"
                    value={textBlurPrice}
                    onChange={(event) => setTextBlurPrice(event.target.value)}
                  />
                </div>
              )}

              {blurredImageIndexes.size > 0 && (
                <div className={styles.priceField}>
                  <label htmlFor="imageBlurPrice">이미지 블러 해제</label>
                  <input
                    id="imageBlurPrice"
                    className={styles.priceInput}
                    type="number"
                    min={0}
                    placeholder="가격 (원, 비우면 판매 안 함)"
                    value={imageBlurPrice}
                    onChange={(event) => setImageBlurPrice(event.target.value)}
                  />
                </div>
              )}
            </div>
          )}

          <button data-action="submit" disabled={submitting} onClick={handleSubmit} type="button">
            <span data-icon data-filled="true">send</span>
            {submitting ? "게시 중..." : "게시하기"}
          </button>
        </div>
      </div>

      {blurPopup && (
        <button
          ref={blurPopupRef}
          type="button"
          className={styles.blurPopup}
          style={{ left: blurPopup.x, top: blurPopup.y }}
          onClick={handleAddBlurRange}
        >
          <span className="material-symbols-outlined">visibility_off</span>
          블러 처리
        </button>
      )}
    </div>
  );
}
