export const CONTENT_VISIBILITY_LABEL: Record<string, string> = {
  PUBLIC: "전체공개",
  BLUR: "블러",
  BLACKBOX: "블랙박스",
  RESTRICTED: "접근제한",
  PARTIAL: "부분공개",
};

export const SORT_OPTIONS = [
  { value: "LATEST", label: "최신순" },
  { value: "LIKES", label: "좋아요순" },
  { value: "COMMENTS", label: "댓글 많은 순" },
  { value: "VIEWS", label: "조회수순" },
] as const;

export const MAX_UPLOAD_SIZE_BYTES = 100 * 1024 * 1024; // 100MB
