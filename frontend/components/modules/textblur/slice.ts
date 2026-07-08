"use client";

import { useCallback } from "react";
import { getPostState, setExtension, usePostStore } from "@/lib/postStore";
import type { BlurRange } from "@/types";

/**
 * [textblur 모듈 - 공용 스토어의 자기 구역(slice) 전용 접근]
 *
 * 텍스트 블러 담당자는 스토어(lib/postStore.ts)를 직접 만지지 않고 이 파일의 훅만 쓴다.
 * → 남의 구역이나 core 필드(title 등)를 실수로 건드릴 방법이 구조적으로 없다.
 *
 * 스토어에 저장되는 모양: extensions.textBlur = { ranges: BlurRange[] }
 * 이 모양이 그대로 백엔드 요청의 extensions.textBlur 로 전송된다.
 */

const KEY = "textBlur";
const EMPTY: BlurRange[] = []; // selector 가 매번 새 배열을 만들지 않도록 상수 재사용

interface TextBlurSection {
  ranges: BlurRange[];
}

/** 자기 구역 읽기 (리렌더 구독) */
export function useBlurRanges(): BlurRange[] {
  return usePostStore((s) => (s.extensions[KEY] as TextBlurSection | undefined)?.ranges ?? EMPTY);
}

/** 자기 구역에 대한 조작 함수 모음 */
export function useBlurActions() {
  const setRanges = useCallback((ranges: BlurRange[]) => {
    setExtension(KEY, { ranges });
  }, []);

  /** 현재 스토어의 ranges 를 읽어(비구독) 새 구간을 병합 추가 */
  const addRange = useCallback((range: BlurRange) => {
    const current = (getPostState().extensions[KEY] as TextBlurSection | undefined)?.ranges ?? [];
    setExtension(KEY, { ranges: mergeRanges([...current, range]) });
  }, []);

  const removeRange = useCallback((index: number) => {
    const current = (getPostState().extensions[KEY] as TextBlurSection | undefined)?.ranges ?? [];
    setExtension(KEY, { ranges: current.filter((_, i) => i !== index) });
  }, []);

  const clear = useCallback(() => setExtension(KEY, { ranges: [] }), []);

  return { setRanges, addRange, removeRange, clear };
}

/** 겹치거나 붙은 구간은 하나로 합친다 */
function mergeRanges(ranges: BlurRange[]): BlurRange[] {
  const sorted = [...ranges].sort((a, b) => a.start - b.start);
  const merged: BlurRange[] = [];
  for (const r of sorted) {
    const last = merged[merged.length - 1];
    if (last && r.start <= last.end) {
      last.end = Math.max(last.end, r.end);
    } else {
      merged.push({ ...r });
    }
  }
  return merged;
}
