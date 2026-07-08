"use client";

import styles from "./MediaUploader.module.css";

// 게시글 작성/수정 시 이미지·영상 다중 첨부 UI.
// lib/image.ts 의 uploadMediaBatch 사용. 확장자/용량(100MB) 검증은 서버에서도 재검증됨.
export default function MediaUploader() {
  return <div className={styles.uploader}>{/* TODO */}</div>;
}
