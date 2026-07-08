"use client";

import { useSyncExternalStore } from "react";
import type { ContentVisibility, PostEditData } from "@/types";

/**
 * [공용 스토어 - core 소유] 작성/수정 화면에서 편집 중인 "게시글 템플릿" 하나를 담는다.
 *
 * 모양은 백엔드 요청/응답 JSON(extensions 포함)과 1:1 이다. 그래서:
 *  - 수정 화면 열기 = GET /posts/{id}/edit 응답을 통째로 hydrate() → 끝 (모듈별 초기화 불필요)
 *  - 저장 = getPostState() 스냅샷을 통째로 전송 → 끝 (안 건드린 모듈 데이터도 자동 보존)
 *
 * 규칙: 모듈은 이 파일을 직접 만지지 말고, 자기 폴더의 slice(예: modules/textblur/slice.ts)
 *       를 통해 extensions 의 자기 구역만 읽고 쓴다. core 필드(title 등)는 core 페이지만 만진다.
 *
 * 주의: 이 스토어는 "작성/수정 화면 전용"이다. 조회(열람) 화면은 서버 완성본을
 *       그대로 렌더링하며 스토어를 쓰지 않는다. (원본의 문지기는 서버라는 원칙)
 */

// ── 게시글 템플릿 ──────────────────────────────────────────────────
export interface PostTemplate {
  title: string;
  content: string;
  visibility: ContentVisibility;
  tags: string[];
  /** 모듈별 구역. key = 모듈 이름 (백엔드 PostModule.key() 와 일치) */
  extensions: Record<string, unknown>;
}

const INITIAL: PostTemplate = {
  title: "",
  content: "",
  visibility: "PUBLIC",
  tags: [],
  extensions: {},
};

// ── 스토어 본체 (라이브러리 없이 React 내장 useSyncExternalStore 사용) ──
let state: PostTemplate = { ...INITIAL, tags: [], extensions: {} };
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((l) => l());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** 현재 스냅샷. 저장(전송) 시 이걸 통째로 body 에 담는다. */
export function getPostState(): PostTemplate {
  return state;
}

/** core 필드 갱신 (title, content 등). 모듈 구역은 setExtension 을 쓸 것. */
export function setPostState(patch: Partial<Omit<PostTemplate, "extensions">>) {
  state = { ...state, ...patch };
  notify();
}

/**
 * [모듈용] extensions 의 자기 구역만 교체한다.
 * 모듈이 남의 구역이나 core 필드를 건드릴 수 없도록 열어둔 유일한 쓰기 통로.
 */
export function setExtension(key: string, value: unknown) {
  state = { ...state, extensions: { ...state.extensions, [key]: value } };
  notify();
}

/** 새 글 작성 화면 진입 시 초기화 */
export function resetPostStore() {
  state = { ...INITIAL, tags: [], extensions: {} };
  notify();
}

/** 수정 화면 진입 시: GET /posts/{id}/edit 응답으로 통째로 채우기 (hydration) */
export function hydratePostStore(data: PostEditData) {
  state = {
    title: data.title,
    content: data.content ?? "",
    visibility: data.visibility,
    tags: data.tags ?? [],
    extensions: { ...data.extensions },
  };
  notify();
}

/**
 * 구독 훅. selector 로 필요한 조각만 선택해서 리렌더 범위를 좁힌다.
 * 예) const title = usePostStore(s => s.title);
 * 주의: selector 가 매번 새 객체/배열을 만들면 무한 리렌더가 나므로,
 *       기본값이 필요하면 모듈 밖의 상수(예: EMPTY_RANGES)를 재사용할 것.
 */
export function usePostStore<T>(selector: (s: PostTemplate) => T): T {
  return useSyncExternalStore(
    subscribe,
    () => selector(state),
    () => selector(state)
  );
}
