import api from "./api";
import type { ApiResponse } from "@/types";

interface MediaUploadResponse {
  url: string;
  mediaType: "IMAGE" | "VIDEO";
  sizeBytes: number;
}

// 단일 파일 업로드 (이미지/영상 공용, 최대 100MB). 백엔드가 Cloudinary 로 전달한다.
export async function uploadMedia(file: File): Promise<MediaUploadResponse> {
  const formData = new FormData();
  formData.append("file", file);
  const res = await api.post<ApiResponse<MediaUploadResponse>>("/api/v1/media/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data.data;
}

export async function uploadMediaBatch(files: File[]): Promise<MediaUploadResponse[]> {
  const formData = new FormData();
  files.forEach((f) => formData.append("files", f));
  const res = await api.post<ApiResponse<MediaUploadResponse[]>>("/api/v1/media/upload/batch", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data.data;
}
