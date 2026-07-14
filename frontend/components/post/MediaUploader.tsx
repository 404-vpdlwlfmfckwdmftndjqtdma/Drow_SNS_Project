"use client";

import { useRef, useState } from "react";
import axios from "axios";
import styles from "./MediaUploader.module.css";
import { uploadMediaBatch } from "@/lib/image";
import type { ApiResponse, MediaType } from "@/types";

export interface MediaItem {
  url: string;
  mediaType: MediaType;
}

const MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 백엔드 MediaService 제한(50MB)과 동일

interface MediaUploaderProps {
  value: MediaItem[];
  onChange: (media: MediaItem[]) => void;
  accept?: string;
}

// 게시글 작성/수정 시 이미지·영상 다중 첨부 UI.
// lib/image.ts 의 uploadMediaBatch 사용. 확장자/용량(50MB) 검증은 서버에서도 재검증됨.
export default function MediaUploader({ value, onChange, accept = "image/*,video/*" }: MediaUploaderProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return;

    const oversized = Array.from(files).find((file) => file.size > MAX_FILE_SIZE_BYTES);
    if (oversized) {
      alert(`${oversized.name} — 파일 용량 제한(50MB)을 초과했습니다.`);
      if (inputRef.current) inputRef.current.value = "";
      return;
    }

    setUploading(true);
    try {
      const uploaded = await uploadMediaBatch(Array.from(files));
      onChange([...value, ...uploaded.map((item) => ({ url: item.url, mediaType: item.mediaType }))]);
    } catch (err) {
      const message = axios.isAxiosError<ApiResponse<unknown>>(err)
        ? err.response?.data?.message
        : undefined;
      alert(message ?? "파일 업로드에 실패했습니다.");
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = "";
    }
  };

  const handleRemove = (index: number) => {
    onChange(value.filter((_, i) => i !== index));
  };

  return (
    <div className={styles.uploader}>
      <label className={styles.dropzone} data-disabled={uploading ? "true" : undefined}>
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          multiple
          hidden
          disabled={uploading}
          onChange={(event) => handleFiles(event.target.files)}
        />
        <span data-icon data-size="large">cloud_upload</span>
        <p>{uploading ? "업로드 중..." : "파일을 드래그하거나 클릭하여 업로드"}</p>
        <p>최대 50MB (JPG, PNG, MP4 등 지원)</p>
      </label>

      {value.length > 0 && (
        <div className={styles.previewList}>
          {value.map((item, index) => (
            <div className={styles.previewItem} key={`${item.url}-${index}`}>
              {item.mediaType === "VIDEO" ? (
                <video className={styles.preview} src={item.url} muted />
              ) : (
                <img className={styles.preview} src={item.url} alt="" />
              )}
              <button type="button" className={styles.removeButton} onClick={() => handleRemove(index)}>
                ✕
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
